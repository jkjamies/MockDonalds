package com.jkjamies.sampleplatter.navint.integration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jkjamies.sampleplatter.features.home.api.navigation.HomeScreen
import com.jkjamies.sampleplatter.features.home.api.ui.HomeTestTags
import com.jkjamies.sampleplatter.features.login.api.navigation.LoginScreen
import com.jkjamies.sampleplatter.features.login.api.navigation.WelcomeScreen
import com.jkjamies.sampleplatter.features.login.api.ui.LoginTestTags
import com.jkjamies.sampleplatter.features.login.api.ui.WelcomeTestTags
import com.jkjamies.sampleplatter.features.more.api.navigation.MoreScreen
import com.jkjamies.sampleplatter.features.more.api.ui.MoreTestTags
import com.jkjamies.sampleplatter.features.profile.api.navigation.ProfileScreen
import com.jkjamies.sampleplatter.features.profile.api.ui.ProfileTestTags
import com.jkjamies.sampleplatter.navint.TestApplication
import com.jkjamies.sampleplatter.navint.setNavIntContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests cross-feature auth state propagation with real Circuit navigation.
 * Uses real presenters with fake data layer — auth state is shared across features.
 */
@RunWith(AndroidJUnit4::class)
class AuthFlowIntegrationTest {

    @get:Rule
    val rule = createComposeRule()

    private val graph
        get() = (InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as TestApplication).graph

    @Test
    fun loginScreenRendersFromDirectNavigation() {
        rule.setNavIntContent(
            circuit = graph.circuit,
            root = LoginScreen(),
        )

        rule.onNodeWithTag(LoginTestTags.BRANDING).assertIsDisplayed()
        rule.onNodeWithTag(LoginTestTags.EMAIL_INPUT).assertIsDisplayed()
        rule.onNodeWithTag(LoginTestTags.SIGN_IN_BUTTON).assertIsDisplayed()
    }

    @Test
    fun profileScreenRendersWhenAuthenticated() {
        // Pre-authenticate
        graph.authManager.login()

        rule.setNavIntContent(
            circuit = graph.circuit,
            root = ProfileScreen,
        )

        rule.onNodeWithTag(ProfileTestTags.NAME).assertIsDisplayed()
        rule.onNodeWithTag(ProfileTestTags.LOGOUT_BUTTON).assertIsDisplayed()

        // Clean up
        graph.authManager.logout()
    }

    @Test
    fun navigateFromMoreToProfileWhenAuthenticated() {
        graph.authManager.login()

        var testNavigator: com.slack.circuit.runtime.Navigator? = null

        rule.setNavIntContent(
            circuit = graph.circuit,
            root = MoreScreen,
            onNavigator = { testNavigator = it },
        )

        rule.onNodeWithTag(MoreTestTags.PROFILE_SECTION).assertIsDisplayed()

        rule.runOnUiThread {
            testNavigator!!.goTo(ProfileScreen)
        }

        rule.waitForIdle()
        rule.onNodeWithTag(ProfileTestTags.NAME).assertIsDisplayed()

        // Clean up
        graph.authManager.logout()
    }

    @Test
    fun signInNavigatesToWelcomeScreen() {
        var testNavigator: com.slack.circuit.runtime.Navigator? = null

        rule.setNavIntContent(
            circuit = graph.circuit,
            root = LoginScreen(),
            onNavigator = { testNavigator = it },
        )

        rule.onNodeWithTag(LoginTestTags.BRANDING).assertIsDisplayed()

        rule.runOnUiThread {
            graph.authManager.login()
            testNavigator!!.goTo(WelcomeScreen())
        }

        rule.waitForIdle()
        rule.onNodeWithTag(WelcomeTestTags.LOGO).assertIsDisplayed()
        rule.onNodeWithTag(WelcomeTestTags.TITLE).assertIsDisplayed()
        rule.onNodeWithTag(WelcomeTestTags.CONTINUE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun welcomeScreenContinueNavigatesBack() {
        var testNavigator: com.slack.circuit.runtime.Navigator? = null

        rule.setNavIntContent(
            circuit = graph.circuit,
            root = LoginScreen(),
            onNavigator = { testNavigator = it },
        )

        rule.runOnUiThread {
            graph.authManager.login()
            testNavigator!!.goTo(WelcomeScreen())
        }

        rule.waitForIdle()
        rule.onNodeWithTag(WelcomeTestTags.CONTINUE_BUTTON).assertIsDisplayed()

        rule.runOnUiThread {
            testNavigator!!.pop()
        }

        rule.waitForIdle()
        rule.onNodeWithTag(LoginTestTags.BRANDING).assertIsDisplayed()
    }

    @Test
    fun moreScreenRendersWithRealPresenter() {
        rule.setNavIntContent(
            circuit = graph.circuit,
            root = MoreScreen,
        )

        rule.onNodeWithTag(MoreTestTags.PROFILE_SECTION).assertIsDisplayed()
        rule.onNodeWithTag(MoreTestTags.MENU_LIST).assertIsDisplayed()
    }
}
