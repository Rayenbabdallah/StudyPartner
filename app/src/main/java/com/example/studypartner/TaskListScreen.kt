package com.example.studypartner

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun TaskListScreen(navController: NavController, viewModel: StudyViewModel) {

    LazyColumn {
        items(viewModel.tasks) { task ->

            val priority = task.difficulty + task.urgency

            val risk = if (task.difficulty >= 4 && task.urgency >= 4) {
                "High Risk"
            } else {
                "Safe"
            }

            Text(
                text = "${task.title} - ${task.subject} | Priority: $priority | $risk"
            )
        }
    }
}