package com.mockdonalds.kiosk.e2e.robots

import android.content.Context
import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

/**
 * Top-level robot for kiosk e2e journey tests. Mirrors the consumer `AppRobot` shape
 * but instruments against `:kioskApp` (`com.mockdonalds.kiosk`).
 *
 * Kiosk has no deep links and no tab nav, so this robot is intentionally smaller
 * than the consumer one — just launch + element actions.
 */
class KioskAppRobot {

    val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    private val targetPackage = InstrumentationRegistry.getInstrumentation().targetContext.packageName
    private val launchTimeout = 10_000L
    private val elementTimeout = 5_000L

    fun launchApp() {
        device.pressHome()
        val intent = context.packageManager.getLaunchIntentForPackage(targetPackage)
            ?: error("Could not find launch intent for $targetPackage")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        device.wait(Until.hasObject(By.pkg(targetPackage).depth(0)), launchTimeout)
    }

    fun waitForElement(testTag: String): Boolean =
        device.wait(Until.hasObject(By.res(testTag)), elementTimeout)

    fun assertElementDisplayed(testTag: String) {
        val found = waitForElement(testTag)
        assert(found) { "Expected element with testTag '$testTag' to be displayed" }
    }

    fun tapElement(testTag: String) {
        assertElementDisplayed(testTag)
        device.findObject(By.res(testTag)).click()
        device.waitForIdle()
    }
}
