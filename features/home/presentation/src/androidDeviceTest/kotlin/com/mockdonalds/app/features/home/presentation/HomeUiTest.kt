package com.mockdonalds.app.features.home.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class HomeUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { HomeUiRobot(composeTestRule) }

    @Test
    fun rendersDefaultState() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test
    fun rendersWithNoPromotion() {
        robot.setContentWithNoPromotion()
        robot.assertScreenWithNoPromotion()
    }

    @Test
    fun rendersWithEmptyCravings() {
        robot.setContentWithEmptyCravings()
        robot.assertScreenWithEmptyCravings()
    }

    @Test
    fun heroCtaButtonEmitsEvent() {
        robot.setDefaultContent()
        robot.tapHeroCtaButton()
        robot.assertLastEvent(HomeEvent.HeroCtaClicked)
    }

    @Test
    fun cravingCardEmitsEvent() {
        robot.setDefaultContent()
        robot.tapCravingCard("1")
        robot.assertLastEvent(HomeEvent.CravingClicked("1"))
    }

    @Test
    fun exploreItemEmitsEvent() {
        robot.setDefaultContent()
        robot.tapExploreItem("1")
        robot.assertLastEvent(HomeEvent.ExploreItemClicked("1"))
    }
}
