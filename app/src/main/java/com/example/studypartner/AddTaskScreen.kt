package com.example.studypartner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AddTaskScreen(navController: NavController, viewModel: StudyViewModel) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(
            value = viewModel.title,
            onValueChange = { viewModel.title = it },
            label = { Text("Task title") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = viewModel.subject,
            onValueChange = { viewModel.subject = it },
            label = { Text("Subject") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LevelPicker(
            label = "Difficulty",
            selected = viewModel.difficulty,
            onSelect = { viewModel.difficulty = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LevelPicker(
            label = "Urgency",
            selected = viewModel.urgency,
            onSelect = { viewModel.urgency = it }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            viewModel.addTask()
            navController.navigate(Screen.Home.route)
        }) {
            Text("Save Task")
        }
    }
}

@Composable
private fun LevelPicker(label: String, selected: Level, onSelect: (Level) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Level.entries.forEach { level ->
                FilterChip(
                    selected = level == selected,
                    onClick = { onSelect(level) },
                    label = { Text(level.label) }
                )
            }
        }
    }
}
