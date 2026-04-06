package com.example.studypartner

sealed class TaskUiState {
    object Loading : TaskUiState()
    data class Success(val tasks: List<StudyTask>) : TaskUiState()
    data class Error(val message: String) : TaskUiState()
}
