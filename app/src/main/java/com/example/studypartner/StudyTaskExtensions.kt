package com.example.studypartner

fun StudyTask.priority(): Int = difficulty + urgency

fun StudyTask.isHighRisk(): Boolean = difficulty >= 4 && urgency >= 4

fun StudyTask.riskLabel(): String = when {
    difficulty >= 4 && urgency >= 4 -> "High Risk"
    difficulty >= 3 || urgency >= 3 -> "Moderate"
    else                            -> "Safe"
}

fun StudyTask.daysUntilDeadline(): Int? = deadline?.let {
    ((it - System.currentTimeMillis()) / (1000L * 60 * 60 * 24)).toInt()
}

fun StudyTask.isOverdue(): Boolean = daysUntilDeadline()?.let { it < 0 } ?: false

fun StudyTask.deadlineLabel(): String = daysUntilDeadline()?.let { days ->
    when {
        days < 0  -> "Overdue by ${-days}d"
        days == 0 -> "Due today!"
        else      -> "Due in ${days}d"
    }
} ?: ""

fun StudyTask.markAsUrgent(): StudyTask = copy(urgency = Level.EXTREME.value)

fun List<StudyTask>.topTask(): StudyTask? = maxByOrNull { it.priority() }

fun List<StudyTask>.highRiskTasks(): List<StudyTask> = filter { it.isHighRisk() }

fun List<StudyTask>.overdueTasks(): List<StudyTask> = filter { it.isOverdue() }
