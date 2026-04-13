package com.example.studypartner

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val instructor: String = "",
    val creditWeight: Float = 0.5f,   // 0–1, used as default gradeWeight for its tasks
    val colorHex: String = "#3949AB", // hex color for UI identification
    val examDate: Long? = null
)

/** Predefined palette users can pick from */
val COURSE_COLOR_PALETTE = listOf(
    "#3949AB", // Indigo
    "#00897B", // Teal
    "#F57C00", // Orange
    "#8E24AA", // Purple
    "#039BE5", // Sky Blue
    "#43A047", // Green
    "#E53935", // Red
    "#FB8C00"  // Amber
)
