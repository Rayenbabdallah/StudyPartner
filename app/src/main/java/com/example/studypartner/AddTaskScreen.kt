package com.example.studypartner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(navController: NavController, viewModel: StudyViewModel) {

    var showTitleError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Task") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = viewModel.title,
                onValueChange = { viewModel.title = it; showTitleError = false },
                label = { Text("Task title") },
                isError = showTitleError,
                supportingText = if (showTitleError) ({ Text("Title is required") }) else null,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.subject,
                onValueChange = { viewModel.subject = it },
                label = { Text("Subject") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = viewModel.deadlineDays,
                onValueChange = { viewModel.deadlineDays = it },
                label = { Text("Days until deadline (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            LevelPicker(
                label = "Difficulty",
                selected = viewModel.difficulty,
                onSelect = { viewModel.difficulty = it }
            )

            LevelPicker(
                label = "Urgency",
                selected = viewModel.urgency,
                onSelect = { viewModel.urgency = it }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (viewModel.title.isBlank()) {
                        showTitleError = true
                    } else {
                        viewModel.addTask()
                        navController.navigate(Screen.Home.route)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Task")
            }
        }
    }
}

@Composable
private fun LevelPicker(label: String, selected: Level, onSelect: (Level) -> Unit) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
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
