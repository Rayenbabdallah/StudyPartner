package com.example.studypartner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController, viewModel: StudyViewModel) {

    val state by viewModel.uiState.collectAsState()

    val advice = when (state) {
        is TaskUiState.Loading -> "Loading..."
        is TaskUiState.Success -> viewModel.getAdvice()
        is TaskUiState.Error   -> (state as TaskUiState.Error).message
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("StudyPartner", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        Text("AI Advice: $advice", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Study load: ${viewModel.studyLoad()} pts",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = { navController.navigate(Screen.Add.route) }) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { navController.navigate(Screen.List.route) }) {
            Text("View Tasks")
        }
    }
}
