package com.mockdonalds.app.bridge

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.navigation.NavStackList
import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * iOS implementation of Circuit's [Navigator] that bridges navigation actions to SwiftUI.
 *
 * ## Why Channel, not StateFlow?
 *
 * Navigation actions are **events** (fire-and-forget commands), not **state** (latest-value-wins).
 * - StateFlow conflates: rapid `pop()` + `goTo()` would drop the pop, since StateFlow only keeps
 *   the latest value. This causes the login screen to remain on the stack after sign-in.
 * - StateFlow replays: the last value is replayed to new/reconnecting subscribers, causing
 *   duplicate navigation on view lifecycle events.
 * - SharedFlow(replay=0) solves replay but intermediate emissions can buffer in the
 *   Kotlin→Swift async bridge, causing already-processed actions to be redelivered.
 *
 * [Channel] provides exactly-once, ordered delivery with no replay and no conflation.
 *
 * ## Run-loop batching
 *
 * On Android, multiple synchronous navigator calls (e.g. `pop()` + `goTo()` in one event handler)
 * are naturally batched — they modify the in-memory backstack, and Compose renders once per frame.
 *
 * On iOS, each navigator call would be a separate Channel emission, causing Swift to process them
 * in separate SwiftUI update cycles. This produces a visible "pop flash" before the push animates.
 *
 * To achieve the same single-frame behavior as Android, actions are accumulated in [pending] during
 * the current main run loop tick. [dispatch_async] to the main queue schedules a flush for the
 * **next** tick — after all synchronous navigator calls in the current event handler have completed.
 * The flush sends the full batch as a single `List<NavigationAction>` through the Channel.
 * Swift processes all actions in one SwiftUI update cycle, producing a seamless transition.
 *
 * ## Buffer capacity
 *
 * Each run loop tick produces at most one batch. Swift consumes batches as fast as the run loop
 * delivers them. A capacity of 5 provides headroom for burst scenarios while still failing fast
 * ([trySend] check) if the consumer falls behind — which would indicate a real bug, not normal load.
 */
class BridgeNavigator : Navigator {
    private val _navigationActions = Channel<List<NavigationAction>>(capacity = 5)

    @NativeCoroutines
    val navigationActions: Flow<List<NavigationAction>> = _navigationActions.receiveAsFlow()

    private val pending = mutableListOf<NavigationAction>()
    private var flushScheduled = false

    /**
     * Accumulates [action] for the current batch and schedules a flush if one isn't pending.
     *
     * All calls within the same synchronous block (e.g. a presenter event handler calling
     * `pop()` then `goTo()`) are collected into [pending]. The first call schedules
     * [dispatch_async] to flush on the next main run loop tick. Subsequent calls see
     * [flushScheduled] is true and only append — no duplicate flushes.
     */
    private fun enqueue(action: NavigationAction) {
        pending.add(action)
        if (!flushScheduled) {
            flushScheduled = true
            dispatch_async(dispatch_get_main_queue()) {
                val batch = pending.toList()
                pending.clear()
                flushScheduled = false
                check(_navigationActions.trySend(batch).isSuccess) {
                    "Navigation consumer is not keeping up — $batch"
                }
            }
        }
    }

    override fun goTo(screen: Screen): Boolean {
        enqueue(NavigationAction.GoTo(screen))
        return true
    }

    override fun pop(result: PopResult?): Screen? {
        enqueue(NavigationAction.Pop)
        return null
    }

    override fun resetRoot(
        newRoot: Screen,
        options: Navigator.StateOptions,
    ): List<Screen> {
        enqueue(NavigationAction.ResetRoot(newRoot))
        return emptyList()
    }

    override fun forward(): Boolean = false

    override fun backward(): Boolean {
        enqueue(NavigationAction.Pop)
        return true
    }

    override fun peek(): Screen? = null

    override fun peekBackStack(): List<Screen> = emptyList()

    override fun peekNavStack(): NavStackList<Screen>? = null

    fun switchTab(tag: String) {
        enqueue(NavigationAction.SwitchTab(tag))
    }

    fun deepLink(screens: List<Screen>) {
        enqueue(NavigationAction.DeepLink(screens))
    }
}
