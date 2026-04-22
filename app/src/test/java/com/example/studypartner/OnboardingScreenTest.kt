package com.example.studypartner

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w400dp-h800dp")
class OnboardingScreenTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun firstPage_showsSmartPriorityTitleAndNextButton() {
        rule.setContent {
            MaterialTheme {
                OnboardingScreen(navController = rememberNavController())
            }
        }
        rule.onNodeWithText("Smart Priority").assertIsDisplayed()
        rule.onNodeWithText("Next").assertIsDisplayed()
        rule.onNodeWithText("Skip").assertIsDisplayed()
    }

    @Test
    fun clickingNext_advancesToSecondPage() {
        rule.setContent {
            MaterialTheme {
                OnboardingScreen(navController = rememberNavController())
            }
        }
        rule.onNodeWithText("Next").performClick()
        rule.waitUntil(timeoutMillis = 3000) {
            rule.onAllNodes(hasText("AI-Powered Advice")).fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("AI-Powered Advice").assertIsDisplayed()
    }
}
