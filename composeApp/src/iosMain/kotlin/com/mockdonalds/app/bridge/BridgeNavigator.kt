package com.mockdonalds.app.bridge

import com.mockdonalds.app.core.circuit.bottomsheet.BottomSheetNavigator
import com.mockdonalds.app.core.circuit.bottomsheet.BottomSheetNavigatorImpl
import com.mockdonalds.app.core.circuit.bottomsheet.BottomSheetRequest
import com.mockdonalds.app.core.circuit.bottomsheet.BottomSheetResult
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.navigation.NavStackList
import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BridgeNavigator : Navigator, BottomSheetNavigator {
    private val _navigationAction = MutableStateFlow<NavigationAction>(NavigationAction.Idle)

    @NativeCoroutinesState
    val navigationAction: StateFlow<NavigationAction> = _navigationAction.asStateFlow()

    // Bottom sheet support — delegates to shared impl, exposes flow for Swift observation
    private val bottomSheetImpl = BottomSheetNavigatorImpl()

    @NativeCoroutinesState
    val bottomSheetRequest: StateFlow<BottomSheetRequest?> = bottomSheetImpl.request

    override fun goTo(screen: Screen): Boolean {
        _navigationAction.value = NavigationAction.GoTo(screen)
        return true
    }

    override fun pop(result: PopResult?): Screen? {
        _navigationAction.value = NavigationAction.Pop
        return null
    }

    override fun resetRoot(
        newRoot: Screen,
        options: Navigator.StateOptions,
    ): List<Screen> {
        _navigationAction.value = NavigationAction.ResetRoot(newRoot)
        return emptyList()
    }

    override fun forward(): Boolean = false

    override fun backward(): Boolean {
        _navigationAction.value = NavigationAction.Pop
        return true
    }

    override fun peek(): Screen? = null

    override fun peekBackStack(): List<Screen> = emptyList()

    override fun peekNavStack(): NavStackList<Screen>? = null

    fun deepLink(screens: List<Screen>) {
        _navigationAction.value = NavigationAction.DeepLink(screens)
    }

    fun consume() {
        _navigationAction.value = NavigationAction.Idle
    }

    // BottomSheetNavigator

    override suspend fun show(screen: Screen): BottomSheetResult =
        bottomSheetImpl.show(screen)

    fun completeBottomSheet(result: BottomSheetResult) = bottomSheetImpl.complete(result)
}
