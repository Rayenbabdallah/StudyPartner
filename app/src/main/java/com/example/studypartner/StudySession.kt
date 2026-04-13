package com.example.studypartner

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val contextType: String,
    val contextLabel: String,
    val taskId: Int? = null,
    val courseId: Int? = null,
    val durationMinutes: Int,
    val breakEveryMinutes: Int,
    val breakLengthMinutes: Int,
    val suggestionReason: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

