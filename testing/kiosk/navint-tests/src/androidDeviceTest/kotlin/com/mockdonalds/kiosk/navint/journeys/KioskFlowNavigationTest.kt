package com.mockdonalds.kiosk.navint.journeys

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mockdonalds.app.features.kiosk.attract.api.navigation.AttractScreen
import com.mockdonalds.app.features.kiosk.attract.api.ui.AttractTestTags
import com.mockdonalds.app.features.kiosk.identify.api.navigation.IdentifyScreen
import com.mockdonalds.app.features.kiosk.identify.api.ui.IdentifyTestTags
import com.mockdonalds.app.features.kiosk.order.api.navigation.KioskOrderScreen
import com.mockdonalds.app.features.kiosk.order.api.ui.KioskOrderTestTags
import com.mockdonalds.kiosk.navint.KioskTestApplication
import com.mockdonalds.kiosk.navint.setKioskNavIntContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke journey covering all three kiosk-flow transitions in one test:
 * Attract.touch → Identify, Identify.skip → KioskOrder, KioskOrder.back → Attract.
 *
 * Uses real presenters wired through the kiosk navint graph; data layer comes from
 * the features/order test fakes (`FakeGetOrderContent`) and feature-specific Fakes
 * in features/kiosk/{attract,identify}/test/.
 *
 * Per-transition isolation tests are a follow-up — this combined journey gives
 * faster signal on the end-to-end navigation contract.
 */
@RunWith(AndroidJUnit4::class)
class KioskFlowNavigationTest {

    @get:Rule
    val rule = createComposeRule()

    private val graph
        get() = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as KioskTestApplication).graph

    @Test
    fun attractToIdentifyToOrderToAttract() {
        rule.setKioskNavIntContent(
            circuit = graph.circuit,
            root = AttractScreen,
        )

        rule.onNodeWithTag(AttractTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(AttractTestTags.TOUCH_TO_ORDER_OVERLAY).assertIsDisplayed()

        rule.onNodeWithTag(AttractTestTags.SCREEN).performClick()
        rule.waitForIdle()

        rule.onNodeWithTag(IdentifyTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(IdentifyTestTags.SKIP).assertIsDisplayed()

        rule.onNodeWithTag(IdentifyTestTags.SKIP).performClick()
        rule.waitForIdle()

        rule.onNodeWithTag(KioskOrderTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.NAV_RAIL).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.CART_BAR).assertIsDisplayed()

        rule.onNodeWithTag(KioskOrderTestTags.BACK_BUTTON).performClick()
        rule.waitForIdle()

        rule.onNodeWithTag(AttractTestTags.SCREEN).assertIsDisplayed()
    }

    @Test
    fun attractScreenStandsAlone() {
        rule.setKioskNavIntContent(
            circuit = graph.circuit,
            root = AttractScreen,
        )

        rule.onNodeWithTag(AttractTestTags.SCREEN).assertIsDisplayed()
    }

    @Test
    fun identifyScreenRendersWithRealPresenter() {
        rule.setKioskNavIntContent(
            circuit = graph.circuit,
            root = IdentifyScreen(next = KioskOrderScreen),
        )

        rule.onNodeWithTag(IdentifyTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(IdentifyTestTags.PHONE_DISPLAY).assertIsDisplayed()
        rule.onNodeWithTag(IdentifyTestTags.QR_SCANNER).assertIsDisplayed()
    }

    @Test
    fun kioskOrderScreenRendersWithRealPresenter() {
        rule.setKioskNavIntContent(
            circuit = graph.circuit,
            root = KioskOrderScreen,
        )

        rule.onNodeWithTag(KioskOrderTestTags.SCREEN).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.NAV_RAIL).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.ITEM_GRID).assertIsDisplayed()
        rule.onNodeWithTag(KioskOrderTestTags.CART_BAR).assertIsDisplayed()
    }
}
