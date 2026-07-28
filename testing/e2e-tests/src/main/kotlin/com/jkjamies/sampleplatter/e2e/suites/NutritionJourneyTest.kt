package com.jkjamies.sampleplatter.e2e.suites

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jkjamies.sampleplatter.e2e.robots.AppRobot
import com.jkjamies.sampleplatter.features.more.api.ui.MoreTestTags
import com.jkjamies.sampleplatter.features.nutrition.api.ui.NutritionTestTags
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Nutrition journey — navigate to Nutrition from More, verify the screen
 * scaffold renders, and navigate back. WebView content rendering is not
 * asserted (hermetic test, can't load remote pages); we verify only that
 * the feature scaffold and navigation work.
 */
@RunWith(AndroidJUnit4::class)
class NutritionJourneyTest {

    private val robot = AppRobot()

    @Before
    fun setUp() {
        robot.launchApp()
    }

    @Test
    fun navigateToNutritionFromMore() {
        robot.tapTab("More")
        robot.assertElementDisplayed(MoreTestTags.MENU_LIST)

        robot.tapElement("${MoreTestTags.MENU_ITEM}-nutrition")
        robot.assertElementDisplayed(NutritionTestTags.SCREEN)
    }

    @Test
    fun navigateBackFromNutritionToMore() {
        robot.tapTab("More")
        robot.tapElement("${MoreTestTags.MENU_ITEM}-nutrition")
        robot.assertElementDisplayed(NutritionTestTags.SCREEN)

        robot.tapElement(NutritionTestTags.BACK_BUTTON)
        robot.assertElementDisplayed(MoreTestTags.MENU_LIST)
    }
}
