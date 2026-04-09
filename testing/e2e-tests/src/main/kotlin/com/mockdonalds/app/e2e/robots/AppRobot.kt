package com.mockdonalds.app.e2e.robots

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

/**
 * Top-level robot for e2e journey tests. Provides common actions:
 * launch the app, navigate tabs, trigger deep links, wait for content.
 *
 * Uses UI Automator for cross-process element access since e2e tests
 * run in a separate process from the app (com.android.test module).
 */
class AppRobot {

    val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().context

    private val targetPackage = "com.mockdonalds.app"
    private val launchTimeout = 10_000L
    private val elementTimeout = 5_000L

    // MARK: - Launch

    fun launchApp() {
        device.pressHome()
        val intent = context.packageManager.getLaunchIntentForPackage(targetPackage)
            ?: error("Could not find launch intent for $targetPackage")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
        device.wait(Until.hasObject(By.pkg(targetPackage).depth(0)), launchTimeout)
    }

    fun launchWithDeepLink(uri: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            setPackage(targetPackage)
        }
        context.startActivity(intent)
        device.wait(Until.hasObject(By.pkg(targetPackage).depth(0)), launchTimeout)
    }

    // MARK: - Tab Navigation

    fun tapTab(label: String) {
        device.findObject(By.text(label.uppercase())).click()
        device.waitForIdle()
    }

    // MARK: - Element Assertions

    fun waitForElement(testTag: String): Boolean {
        return device.wait(Until.hasObject(By.desc(testTag)), elementTimeout)
    }

    fun assertElementDisplayed(testTag: String) {
        val found = waitForElement(testTag)
        assert(found) { "Expected element with testTag '$testTag' to be displayed" }
    }

    fun assertElementNotDisplayed(testTag: String) {
        val element = device.findObject(By.desc(testTag))
        assert(element == null) { "Expected element with testTag '$testTag' to NOT be displayed" }
    }

    // MARK: - Interaction

    fun tapElement(testTag: String) {
        assertElementDisplayed(testTag)
        device.findObject(By.desc(testTag)).click()
        device.waitForIdle()
    }

    fun typeText(testTag: String, text: String) {
        assertElementDisplayed(testTag)
        val element = device.findObject(By.desc(testTag))
        element.click()
        element.text = text
        device.waitForIdle()
    }
}
