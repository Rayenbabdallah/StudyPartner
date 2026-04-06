package com.example.studypartner

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    var title by mutableStateOf("")
    var subject by mutableStateOf("")
    var difficulty by mutableStateOf("")
    var urgency by mutableStateOf("")

    var tasks by mutableStateOf(listOf<StudyTask>())
        private set

    private var isLoading by mutableStateOf(true)

    val uiState: TaskUiState
        get() = if (isLoading) TaskUiState.Loading else TaskUiState.Success(tasks)

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.taskDao()

    init {
        viewModelScope.launch {
            dao.getAllTasks().collect {
                tasks = it
                isLoading = false
            }
        }
    }

    fun addTask() {
        if (title.isNotBlank() && difficulty.isNotBlank() && urgency.isNotBlank()) {

            val newTask = StudyTask(
                title = title,
                subject = subject,
                difficulty = difficulty.toInt(),
                urgency = urgency.toInt()
            )

            viewModelScope.launch {
                dao.insertTask(newTask)
            }

            title = ""
            subject = ""
            difficulty = ""
            urgency = ""
        }
    }

    fun getAdvice(): String {
        if (tasks.isEmpty()) return "Add tasks to get advice"

        val highRiskTasks = tasks.filter { it.difficulty >= 4 && it.urgency >= 4 }

        return if (highRiskTasks.isNotEmpty()) {
            "You have high-risk tasks. Focus on them first"
        } else {
            val topTask = tasks.maxByOrNull { it.difficulty + it.urgency }
            if (topTask != null) {
                "Start with ${topTask.subject}"
            } else {
                "Good balance"
            }
        }
    }
}