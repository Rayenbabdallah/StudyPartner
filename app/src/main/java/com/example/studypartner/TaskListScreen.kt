package com.example.studypartner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            items(viewModel.sortedTasks()) { task ->
                val (_, title, subject) = task

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$title - $subject\nPriority: ${task.priority()} | ${task.riskLabel()}",
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = { viewModel.markAsUrgent(task) }) {
                        Text("!")
                    }
                }
            }
        }

        is TaskUiState.Error -> Text(text = state.message)
    }
}
