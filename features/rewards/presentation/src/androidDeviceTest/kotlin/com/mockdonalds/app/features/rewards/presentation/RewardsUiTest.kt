package com.mockdonalds.app.features.rewards.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test

class RewardsUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { RewardsUiRobot(composeTestRule) }

    @Test
    fun rendersDefaultState() {
        robot.setDefaultContent()
        robot.assertDefaultScreen()
    }

    @Test
    fun rendersWithNoProgress() {
        robot.setContentWithNoProgress()
        robot.assertScreenWithNoProgress()
    }

    @Test
    fun rendersWithEmptyVault() {
        robot.setContentWithEmptyVault()
        robot.assertScreenWithEmptyVault()
    }

    @Test
    fun rendersWithEmptyHistory() {
        robot.setContentWithEmptyHistory()
        robot.assertScreenWithEmptyHistory()
    }

    @Test
    fun viewAllEmitsEvent() {
        robot.setDefaultContent()
        robot.tapViewAll()
        robot.assertLastEvent(RewardsEvent.ViewAllClicked)
    }

    @Test
    fun featuredVaultCardEmitsEvent() {
        robot.setDefaultContent()
        robot.tapFeaturedVaultCard("1")
        robot.assertLastEvent(RewardsEvent.VaultSpecialClicked("1"))
    }

    @Test
    fun vaultSpecialCardEmitsEvent() {
        robot.setDefaultContent()
        robot.tapVaultSpecialCard("2")
        robot.assertLastEvent(RewardsEvent.VaultSpecialClicked("2"))
    }
}
