package com.example.studypartner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen() {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf("") }

    var tasks by remember { mutableStateOf(listOf<StudyTask>()) }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "StudyPartner")

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Task title") }
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") }
        )

        OutlinedTextField(
            value = difficulty,
            onValueChange = { difficulty = it },
            label = { Text("Difficulty (1-5)") }
        )

        OutlinedTextField(
            value = urgency,
            onValueChange = { urgency = it },
            label = { Text("Urgency (1-5)") }
        )

        Button(onClick = {
            if (title.isNotBlank() && difficulty.isNotBlank() && urgency.isNotBlank()) {
                val newTask = StudyTask(
                    title = title,
                    subject = subject,
                    difficulty = difficulty.toInt(),
                    urgency = urgency.toInt()
                )

                tasks = tasks + newTask

                title = ""
                subject = ""
                difficulty = ""
                urgency = ""
            }
        }) {
            Text("Add Task")
        }

        LazyColumn {
            items(tasks) { task ->

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