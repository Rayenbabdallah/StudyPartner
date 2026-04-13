package com.example.studypartner

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val courseId: Int? = null,
    val difficulty: Int,
    val urgency: Int,
    val deadline: Long? = null,
    val type: String = TaskType.ASSIGNMENT.name,
    val gradeWeight: Float = GradeImpact.MEDIUM.weight,
    val progress: Int = 0          // 0–100; replaces isCompleted boolean
)
