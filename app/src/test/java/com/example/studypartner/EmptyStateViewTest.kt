package com.example.studypartner

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w400dp-h800dp")
class EmptyStateViewTest {

    @get:Rule
    val rule = createComposeRule()

    @Test
    fun rendersTitleAndSubtitle() {
        rule.setContent {
            MaterialTheme {
                EmptyStateView(
                    icon = Icons.Default.Inbox,
                    title = "Nothing here yet",
                    subtitle = "Add your first task to get started",
                )
            }
        }
        rule.onNodeWithText("Nothing here yet").assertIsDisplayed()
        rule.onNodeWithText("Add your first task to get started").assertIsDisplayed()
    }

    @Test
    fun actionButton_clickInvokesCallback() {
        var clicked = false
        rule.setContent {
            MaterialTheme {
                EmptyStateView(
                    icon = Icons.Default.Inbox,
                    title = "t",
                    subtitle = "s",
                    actionLabel = "Add",
                    onAction = { clicked = true },
                )
            }
        }
        rule.onNodeWithText("Add").assertIsDisplayed().performClick()
        assertTrue(clicked)
    }

    @Test
    fun actionButton_absentWhenLabelNull() {
        rule.setContent {
            MaterialTheme {
                EmptyStateView(
                    icon = Icons.Default.Inbox,
                    title = "t",
                    subtitle = "s",
                )
            }
        }
        rule.onAllNodesWithText("Add").assertCountEquals(0)
    }
}
