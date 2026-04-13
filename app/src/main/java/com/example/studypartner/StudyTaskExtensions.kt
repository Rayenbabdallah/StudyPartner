package com.example.studypartner

// ── Computed from progress ────────────────────────────────────────────────────
val StudyTask.isCompleted: Boolean get() = progress >= 100

// ── Risk ──────────────────────────────────────────────────────────────────────
fun StudyTask.priority(): Int = difficulty + urgency

fun StudyTask.isHighRisk(): Boolean = difficulty >= 4 && urgency >= 4

fun StudyTask.riskLabel(): String = when {
    difficulty >= 4 && urgency >= 4 -> "High Risk"
    difficulty >= 3 || urgency >= 3 -> "Moderate"
    else                            -> "Safe"
}

// ── Priority score label (0–100 scale) ───────────────────────────────────────
fun StudyTask.priorityLabel(): String = when {
    score() >= 80 -> "Critical"
    score() >= 60 -> "High"
    score() >= 40 -> "Medium"
    else          -> "Low"
}

// ── Deadline ──────────────────────────────────────────────────────────────────
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

// ── Type ──────────────────────────────────────────────────────────────────────
val StudyTask.taskType: TaskType get() = TaskType.fromName(type)

// ── Grade impact ──────────────────────────────────────────────────────────────
val StudyTask.gradeImpact: GradeImpact get() = GradeImpact.fromWeight(gradeWeight)

// ── Score (delegates to engine) ───────────────────────────────────────────────
fun StudyTask.score(): Double = PriorityEngine.score(this)

// ── Mutations ─────────────────────────────────────────────────────────────────
fun StudyTask.markAsUrgent(): StudyTask    = copy(urgency = Level.EXTREME.value)
fun StudyTask.toggleCompleted(): StudyTask = copy(progress = if (isCompleted) 0 else 100)
fun StudyTask.withProgress(pct: Int): StudyTask = copy(progress = pct.coerceIn(0, 100))

// ── List helpers ──────────────────────────────────────────────────────────────
fun List<StudyTask>.topTask(): StudyTask? = maxByOrNull { it.priority() }
fun List<StudyTask>.highRiskTasks(): List<StudyTask>  = filter { it.isHighRisk() }
fun List<StudyTask>.overdueTasks(): List<StudyTask>   = filter { it.isOverdue() }
fun List<StudyTask>.activeTasks(): List<StudyTask>    = filter { !it.isCompleted }
fun List<StudyTask>.criticalTasks(): List<StudyTask>  = filter { it.score() >= 80 && !it.isCompleted }
