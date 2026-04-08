package com.mockdonalds.app.core.circuit.bottomsheet

import androidx.compose.runtime.staticCompositionLocalOf
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Cross-platform bottom sheet navigator that presenters use to present a full
 * [Screen] as a modal bottom sheet. [show] suspends until the sheet is dismissed,
 * returning the [BottomSheetResult].
 *
 * On Android, the host composable observes [BottomSheetNavigatorImpl.request] and
 * renders a `ModalBottomSheet` with `CircuitContent(screen)`. On iOS, [BridgeNavigator]
 * delegates to this and exposes the request flow via `@NativeCoroutinesState` for
 * SwiftUI to observe and present via `.sheet`.
 */
interface BottomSheetNavigator {
    suspend fun show(screen: Screen): BottomSheetResult
}

sealed class BottomSheetResult {
    data object Confirmed : BottomSheetResult()
    data object Dismissed : BottomSheetResult()
}

val LocalBottomSheetNavigator = staticCompositionLocalOf<BottomSheetNavigator> {
    object : BottomSheetNavigator {
        override suspend fun show(screen: Screen): BottomSheetResult = BottomSheetResult.Dismissed
    }
}

/**
 * Shared implementation backed by [MutableStateFlow] + [CompletableDeferred].
 *
 * - Android: `App.kt` collects [request] and renders `ModalBottomSheet`.
 * - iOS: [BridgeNavigator] wraps this and exposes [request] via `@NativeCoroutinesState`.
 *
 * When the sheet is dismissed, the host calls [complete] which completes the
 * pending deferred and clears the request — resuming the presenter's suspend call.
 */
class BottomSheetNavigatorImpl : BottomSheetNavigator {
    private val _request = MutableStateFlow<BottomSheetRequest?>(null)
    val request: StateFlow<BottomSheetRequest?> = _request.asStateFlow()

    private var pendingResult: CompletableDeferred<BottomSheetResult>? = null

    override suspend fun show(screen: Screen): BottomSheetResult {
        val deferred = CompletableDeferred<BottomSheetResult>()
        pendingResult = deferred
        _request.value = BottomSheetRequest(screen)
        return deferred.await()
    }

    fun complete(result: BottomSheetResult) {
        pendingResult?.complete(result)
        pendingResult = null
        _request.value = null
    }
}

data class BottomSheetRequest(val screen: Screen)
