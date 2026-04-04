package com.example.studypartner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {

                    composable("home") {
                        HomeScreen(navController, viewModel)
                    }

                    composable("add") {
                        AddTaskScreen(navController, viewModel)
                    }

                    composable("list") {
                        TaskListScreen(navController, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: StudyViewModel) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "StudyPartner")

        OutlinedTextField(
            value = viewModel.title,
            onValueChange = { viewModel.title = it },
            label = { Text("Task title") }
        )

        OutlinedTextField(
            value = viewModel.subject,
            onValueChange = { viewModel.subject = it },
            label = { Text("Subject") }
        )

        OutlinedTextField(
            value = viewModel.difficulty,
            onValueChange = { viewModel.difficulty = it },
            label = { Text("Difficulty (1-5)") }
        )

        OutlinedTextField(
            value = viewModel.urgency,
            onValueChange = { viewModel.urgency = it },
            label = { Text("Urgency (1-5)") }
        )

        Button(onClick = { viewModel.addTask() }) {
            Text("Add Task")
        }

        Text(text = "AI Advice: ${viewModel.getAdvice()}")

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
}