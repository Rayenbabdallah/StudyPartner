package com.example.studypartner

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies the navigation graph wires the [Screen] route definitions correctly:
 *   - parameter-less routes are reachable
 *   - parameterised routes (e.g. task_detail/{taskId}) extract the argument
 *   - back navigation pops to the previous destination
 *
 * Uses a stub NavHost (real screens depend on the ViewModel + DB and are out of scope here).
 * Compose tests work on the JVM via Robolectric.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NavigationSmokeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Composable
    private fun StubGraph(navController: TestNavHostController) {
        NavHost(navController = navController, startDestination = Screen.Home.route) {
            composable(Screen.Home.route)         { Text("HOME-SCREEN") }
            composable(Screen.List.route)         { Text("LIST-SCREEN") }
            composable(Screen.AiAssistant.route)  { Text("AI-SCREEN") }
            composable(Screen.Settings.route)     { Text("SETTINGS-SCREEN") }
            composable(
                route = Screen.TaskDetail.route,
                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("taskId") ?: -1
                Text("TASK-DETAIL-$id")
            }
            composable(
                route = Screen.Focus.route,
                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("taskId") ?: -1
                Text("FOCUS-$id")
            }
        }
    }

    @Test
    fun startDestination_isHome() {
        lateinit var nav: TestNavHostController
        composeTestRule.setContent {
            nav = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
                navigatorProvider.addNavigator(androidx.navigation.compose.ComposeNavigator())
            }
            StubGraph(nav)
        }
        composeTestRule.onNodeWithText("HOME-SCREEN").assertIsDisplayed()
        assertEquals(Screen.Home.route, nav.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun navigateTo_taskList_thenAi_thenSettings_routesUpdate() {
        lateinit var nav: TestNavHostController
        composeTestRule.setContent {
            nav = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
                navigatorProvider.addNavigator(androidx.navigation.compose.ComposeNavigator())
            }
            StubGraph(nav)
        }

        composeTestRule.runOnUiThread { nav.navigate(Screen.List.route) }
        composeTestRule.onNodeWithText("LIST-SCREEN").assertIsDisplayed()
        assertEquals(Screen.List.route, nav.currentBackStackEntry?.destination?.route)

        composeTestRule.runOnUiThread { nav.navigate(Screen.AiAssistant.route) }
        composeTestRule.onNodeWithText("AI-SCREEN").assertIsDisplayed()
        assertEquals(Screen.AiAssistant.route, nav.currentBackStackEntry?.destination?.route)

        composeTestRule.runOnUiThread { nav.navigate(Screen.Settings.route) }
        composeTestRule.onNodeWithText("SETTINGS-SCREEN").assertIsDisplayed()
        assertEquals(Screen.Settings.route, nav.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun parameterisedRoute_taskDetail_extractsId() {
        lateinit var nav: TestNavHostController
        composeTestRule.setContent {
            nav = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
                navigatorProvider.addNavigator(androidx.navigation.compose.ComposeNavigator())
            }
            StubGraph(nav)
        }
        composeTestRule.runOnUiThread {
            nav.navigate(Screen.TaskDetail.createRoute(42))
        }
        composeTestRule.onNodeWithText("TASK-DETAIL-42").assertIsDisplayed()
        assertEquals(Screen.TaskDetail.route, nav.currentBackStackEntry?.destination?.route)
        assertEquals(42, nav.currentBackStackEntry?.arguments?.getInt("taskId"))
    }

    @Test
    fun parameterisedRoute_focus_extractsId() {
        lateinit var nav: TestNavHostController
        composeTestRule.setContent {
            nav = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
                navigatorProvider.addNavigator(androidx.navigation.compose.ComposeNavigator())
            }
            StubGraph(nav)
        }
        composeTestRule.runOnUiThread { nav.navigate(Screen.Focus.createRoute(7)) }
        composeTestRule.onNodeWithText("FOCUS-7").assertIsDisplayed()
        assertEquals(7, nav.currentBackStackEntry?.arguments?.getInt("taskId"))
    }

    @Test
    fun popBackStack_returnsToPreviousDestination() {
        lateinit var nav: TestNavHostController
        composeTestRule.setContent {
            nav = TestNavHostController(ApplicationProvider.getApplicationContext()).apply {
                navigatorProvider.addNavigator(androidx.navigation.compose.ComposeNavigator())
            }
            StubGraph(nav)
        }
        composeTestRule.runOnUiThread { nav.navigate(Screen.List.route) }
        composeTestRule.onNodeWithText("LIST-SCREEN").assertIsDisplayed()

        composeTestRule.runOnUiThread { nav.popBackStack() }
        composeTestRule.onNodeWithText("HOME-SCREEN").assertIsDisplayed()
        assertEquals(Screen.Home.route, nav.currentBackStackEntry?.destination?.route)
    }
}
