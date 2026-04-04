package com.example.studypartner

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController, viewModel: StudyViewModel) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("StudyPartner")

        Spacer(modifier = Modifier.height(16.dp))

        Text("AI Advice: ${viewModel.getAdvice()}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("add") }) {
            Text("Add Task")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = { navController.navigate("list") }) {
            Text("View Tasks")
        }
    }
}