package com.mockdonalds.app.e2e.suites

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mockdonalds.app.e2e.robots.AppRobot
import com.mockdonalds.app.features.more.api.ui.MoreTestTags
import com.mockdonalds.app.features.recents.api.ui.RecentsTestTags
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Recents journey — navigate to Recents from More, verify content,
 * and navigate back.
 */
@RunWith(AndroidJUnit4::class)
class RecentsJourneyTest {

    private val robot = AppRobot()

    @Before
    fun setUp() {
        robot.launchApp()
    }

    @Test
    fun navigateToRecentsFromMore() {
        robot.tapTab("More")
        robot.assertElementDisplayed(MoreTestTags.MENU_LIST)

        robot.tapElement("${MoreTestTags.MENU_ITEM}-1")
        robot.assertElementDisplayed(RecentsTestTags.SCREEN)
        robot.assertElementDisplayed(RecentsTestTags.LIST)
    }

    @Test
    fun navigateBackFromRecentsToMore() {
        robot.tapTab("More")
        robot.tapElement("${MoreTestTags.MENU_ITEM}-1")
        robot.assertElementDisplayed(RecentsTestTags.SCREEN)

        robot.tapElement(RecentsTestTags.BACK_BUTTON)
        robot.assertElementDisplayed(MoreTestTags.MENU_LIST)
    }
}
