package com.example.studypartner

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun TaskListScreen(navController: NavController, viewModel: StudyViewModel) {

    when (val state = viewModel.uiState) {

        is TaskUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        is TaskUiState.Success -> LazyColumn {
            items(state.tasks) { task ->
                Text(text = "${task.title} - ${task.subject} | Priority: ${task.priority()} | ${task.riskLabel()}")
            }
        }

        is TaskUiState.Error -> Text(text = state.message)
    }
}
