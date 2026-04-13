package com.example.studypartner

sealed class AiState {
    object Idle    : AiState()
    object Loading : AiState()
    data class Success(val response: String) : AiState()
    data class Failure(val message: String) : AiState()
    object Unavailable : AiState()
}
