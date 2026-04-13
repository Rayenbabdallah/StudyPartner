package com.example.studypartner

enum class TaskType(val label: String) {
    ASSIGNMENT("Assignment"),
    EXAM("Exam"),
    PROJECT("Project"),
    READING("Reading"),
    REVIEW("Review"),
    QUIZ("Quiz");

    companion object {
        fun fromName(name: String): TaskType =
            entries.firstOrNull { it.name == name } ?: ASSIGNMENT
    }
}
