package com.mockdonalds.kiosk.e2e.suites

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mockdonalds.app.features.kiosk.attract.api.ui.AttractTestTags
import com.mockdonalds.app.features.kiosk.identify.api.ui.IdentifyTestTags
import com.mockdonalds.app.features.kiosk.order.api.ui.KioskOrderTestTags
import com.mockdonalds.kiosk.e2e.robots.KioskAppRobot
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke journey for the kiosk app — launches `:kioskApp` and walks the full
 * Attract → Identify (skip) → KioskOrder → back-to-Attract flow on a real device.
 *
 * Per-step / per-failure-path tests are a follow-up; this combined journey gives
 * fastest signal on the end-to-end installed APK behaviour.
 */
@RunWith(AndroidJUnit4::class)
class KioskFlowJourneyTest {

    private val robot = KioskAppRobot()

    @Before
    fun setUp() {
        robot.launchApp()
    }

    @Test
    fun fullKioskFlow() {
        // Attract is the launch root.
        robot.assertElementDisplayed(AttractTestTags.SCREEN)
        robot.assertElementDisplayed(AttractTestTags.TOUCH_TO_ORDER_OVERLAY)

        // Tap anywhere on attract → IdentifyScreen.
        robot.tapElement(AttractTestTags.SCREEN)
        robot.assertElementDisplayed(IdentifyTestTags.SCREEN)
        robot.assertElementDisplayed(IdentifyTestTags.SKIP)

        // Skip identification → KioskOrderScreen.
        robot.tapElement(IdentifyTestTags.SKIP)
        robot.assertElementDisplayed(KioskOrderTestTags.SCREEN)
        robot.assertElementDisplayed(KioskOrderTestTags.NAV_RAIL)
        robot.assertElementDisplayed(KioskOrderTestTags.CART_BAR)

        // Back from KioskOrder → Attract (resetRoot).
        robot.tapElement(KioskOrderTestTags.BACK_BUTTON)
        robot.assertElementDisplayed(AttractTestTags.SCREEN)
    }
}
