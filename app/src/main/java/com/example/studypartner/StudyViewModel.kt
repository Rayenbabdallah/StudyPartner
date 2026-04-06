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
    var difficulty by mutableStateOf(Level.LOW)
    var urgency by mutableStateOf(Level.LOW)

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
        if (title.isNotBlank()) {
            val newTask = StudyTask(
                title = title,
                subject = subject,
                difficulty = difficulty.value,
                urgency = urgency.value
            )

            viewModelScope.launch {
                dao.insertTask(newTask)
            }

            title = ""
            subject = ""
            difficulty = Level.LOW
            urgency = Level.LOW
        }
    }

    fun getAdvice(): String = PriorityEngine.getAdvice(tasks)

    fun sortedTasks(): List<StudyTask> = PriorityEngine.sortByPriority(tasks)

    fun studyLoad(): Int = PriorityEngine.studyLoad(tasks)
}