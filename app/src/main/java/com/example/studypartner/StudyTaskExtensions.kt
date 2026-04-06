package com.example.studypartner

fun StudyTask.priority(): Int = difficulty + urgency

fun StudyTask.isHighRisk(): Boolean = difficulty >= 4 && urgency >= 4

fun StudyTask.riskLabel(): String = when {
    difficulty >= 4 && urgency >= 4 -> "High Risk"
    difficulty >= 3 || urgency >= 3 -> "Moderate"
    else                            -> "Safe"
}

fun List<StudyTask>.topTask(): StudyTask? = maxByOrNull { it.priority() }

fun List<StudyTask>.highRiskTasks(): List<StudyTask> = filter { it.isHighRisk() }
