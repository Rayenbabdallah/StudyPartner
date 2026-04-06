package com.example.studypartner

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    var title by mutableStateOf("")
    var subject by mutableStateOf("")
    var difficulty by mutableStateOf(Level.LOW)
    var urgency by mutableStateOf(Level.LOW)
    var deadlineDays by mutableStateOf("")

    private val repository = TaskRepository(
        AppDatabase.getDatabase(application).taskDao()
    )

    val uiState: StateFlow<TaskUiState> = repository.allTasks
        .map { TaskUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TaskUiState.Loading
        )

    private val tasks: List<StudyTask>
        get() = (uiState.value as? TaskUiState.Success)?.tasks ?: emptyList()

    fun addTask() {
        if (title.isNotBlank()) {
            val deadlineMillis = deadlineDays.toIntOrNull()?.let { days ->
                System.currentTimeMillis() + days * 24L * 60 * 60 * 1000
            }

            val newTask = StudyTask(
                title = title,
                subject = subject,
                difficulty = difficulty.value,
                urgency = urgency.value,
                deadline = deadlineMillis
            )

            viewModelScope.launch { repository.insert(newTask) }

            title = ""
            subject = ""
            difficulty = Level.LOW
            urgency = Level.LOW
            deadlineDays = ""
        }
    }

    fun markAsUrgent(task: StudyTask) {
        viewModelScope.launch { repository.update(task.markAsUrgent()) }
    }

    fun deleteTask(task: StudyTask) {
        viewModelScope.launch { repository.delete(task) }
    }

    fun getAdvice(): String = PriorityEngine.getAdvice(tasks)

    fun sortedTasks(): List<StudyTask> = PriorityEngine.sortByPriority(tasks)

    fun studyLoad(): Int = PriorityEngine.studyLoad(tasks)
}
