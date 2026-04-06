package com.example.studypartner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.studypartner.ui.theme.StudyPartnerTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyPartnerTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = Screen.Home.route) {

                    composable(Screen.Home.route) {
                        HomeScreen(navController, viewModel)
                    }

                    composable(Screen.Add.route) {
                        AddTaskScreen(navController, viewModel)
                    }

                    composable(Screen.List.route) {
                        TaskListScreen(navController, viewModel)
                    }
                }
            }
        }
    }
}
