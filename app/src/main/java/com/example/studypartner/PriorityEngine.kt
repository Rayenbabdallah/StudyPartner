package com.example.studypartner

/**
 * Adaptive Priority Engine
 *
 * Produces a normalized 0–100 score using six factors:
 *
 *   0.30 × urgency_norm        (time pressure, Level 1–5 → 0–1)
 *   0.25 × grade_impact        (gradeWeight field, 0–1)
 *   0.15 × difficulty_norm     (cognitive effort, Level 1–5 → 0–1)
 *   0.10 × low_progress        ((100 – progress) / 100)
 *   0.10 × deadline_proximity  (how near the deadline is, 0–1)
 *   0.10 × overload_factor     (urgency mirrored to penalise accumulation)
 *
 * Score bands: Critical ≥ 80 · High ≥ 60 · Medium ≥ 40 · Low < 40
 */
object PriorityEngine {

    // ── Core score ────────────────────────────────────────────────────────────
    fun score(task: StudyTask): Double {
        val urgencyNorm       = task.urgency / 5.0
        val gradeImpact       = task.gradeWeight.toDouble()
        val difficultyNorm    = task.difficulty / 5.0
        val lowProgressFactor = (100 - task.progress) / 100.0
        val deadlineProximity = deadlineScore(task)
        val overloadFactor    = urgencyNorm            // mirrors urgency as accumulation penalty

        return (0.30 * urgencyNorm +
                0.25 * gradeImpact +
                0.15 * difficultyNorm +
                0.10 * lowProgressFactor +
                0.10 * deadlineProximity +
                0.10 * overloadFactor) * 100.0
    }

    private fun deadlineScore(task: StudyTask): Double = when {
        task.isOverdue()                                      -> 1.0
        task.daysUntilDeadline()?.let { it <= 1  } == true  -> 0.9
        task.daysUntilDeadline()?.let { it <= 3  } == true  -> 0.7
        task.daysUntilDeadline()?.let { it <= 7  } == true  -> 0.5
        task.daysUntilDeadline()?.let { it <= 14 } == true  -> 0.3
        task.daysUntilDeadline()?.let { it <= 30 } == true  -> 0.1
        else                                                  -> 0.0
    }

    // ── Sorting ───────────────────────────────────────────────────────────────
    fun sortByPriority(tasks: List<StudyTask>): List<StudyTask> =
        tasks.sortedWith(
            compareBy<StudyTask> { it.isCompleted }
                .thenByDescending { score(it) }
        )

    // ── Aggregates ────────────────────────────────────────────────────────────
    fun studyLoad(tasks: List<StudyTask>): Int =
        tasks.filter { !it.isCompleted }.sumOf { it.priority() }

    fun recommendedSessionMinutes(tasks: List<StudyTask>): Int =
        when (studyLoad(tasks)) {
            0         -> 0
            in 1..8   -> 30
            in 9..16  -> 60
            in 17..24 -> 90
            else      -> 120
        }

    fun subjectBreakdown(tasks: List<StudyTask>): Map<String, Double> =
        tasks
            .filter { it.subject.isNotBlank() }
            .groupBy { it.subject }
            .mapValues { (_, group) -> group.sumOf { score(it) } }
            .entries
            .sortedByDescending { it.value }
            .associate { it.key to it.value }

    // ── Course-level risk score (0–100 average) ───────────────────────────────
    fun courseRiskScore(tasks: List<StudyTask>, course: Course): Double {
        val courseTasks = tasks.filter { it.courseId == course.id && !it.isCompleted }
        if (courseTasks.isEmpty()) return 0.0

        val avgTaskScore = courseTasks.map { score(it) }.average()

        // Bonus if exam is near
        val examBonus = course.examDate?.let { examMillis ->
            val days = ((examMillis - System.currentTimeMillis()) / (1000L * 60 * 60 * 24)).toInt()
            when {
                days < 0  -> 20.0
                days <= 3 -> 15.0
                days <= 7 -> 10.0
                days <= 14 -> 5.0
                else       -> 0.0
            }
        } ?: 0.0

        return (avgTaskScore + examBonus).coerceAtMost(100.0)
    }

    // ── Panic / Ghasret Lekleb detection ─────────────────────────────────────
    /**
     * Returns true when the student is in last-minute panic mode.
     * Trigger A: any single task has ≤ 2 days left, < 50% progress, grade weight ≥ 0.5
     * Trigger B: 3+ tasks have ≤ 3 days left and < 70% progress
     */
    fun isPanicMode(tasks: List<StudyTask>): Boolean {
        val active = tasks.filter { !it.isCompleted }
        val triggerA = active.any { t ->
            val d = t.daysUntilDeadline()
            d != null && d in 0..2 && t.progress < 50 && t.gradeWeight >= 0.5f
        }
        val triggerB = active.count { t ->
            val d = t.daysUntilDeadline()
            d != null && d in 0..3 && t.progress < 70
        } >= 3
        return triggerA || triggerB
    }

    fun panicTasks(tasks: List<StudyTask>): List<StudyTask> =
        tasks.filter { !it.isCompleted }.filter { t ->
            val d = t.daysUntilDeadline()
            (d != null && d in 0..2 && t.progress < 50 && t.gradeWeight >= 0.5f) ||
            (d != null && d in 0..3 && t.progress < 70)
        }.sortedByDescending { survivalScore(it) }

    // ── Survival Priority Engine ──────────────────────────────────────────────
    /**
     * "What gives the most marks in the least time?"
     * Favours high grade impact + near deadline + lower difficulty (faster to finish).
     */
    fun survivalScore(task: StudyTask): Double {
        val gradeImpact  = task.gradeWeight.toDouble()
        val dlScore      = deadlineScore(task)
        val easiness     = 1.0 - (task.difficulty / 5.0)
        return (gradeImpact * 0.5 + dlScore * 0.4 + easiness * 0.1) * 100.0
    }

    fun sortBySurvival(tasks: List<StudyTask>): List<StudyTask> =
        tasks.filter { !it.isCompleted }.sortedByDescending { survivalScore(it) }

    fun survivalLabel(task: StudyTask, strings: StringSet = AppStrings.normal): String = when {
        survivalScore(task) >= 70 -> strings.doNow
        survivalScore(task) >= 40 -> strings.ifTime
        else                      -> strings.skip
    }

    // ── Minimum Viable Work ───────────────────────────────────────────────────
    fun minimumViableWork(task: StudyTask): String = when (task.taskType) {
        TaskType.EXAM       -> "Focus on high-weight topics only. Skip optional chapters."
        TaskType.ASSIGNMENT -> "Aim for 70% complete. Submit something over nothing."
        TaskType.PROJECT    -> "Deliver the core requirement. Drop extras and polish."
        TaskType.READING    -> "Read summaries and key sections only."
        TaskType.REVIEW     -> "Review past papers and key formulas only."
        TaskType.QUIZ       -> "Practice the most common question types. Speed over depth."
    }

    // ── Risk onset (how long ago did this task become critical?) ─────────────
    fun riskOnsetDays(task: StudyTask): Int? {
        val days = task.daysUntilDeadline() ?: return null
        if (days > 7) return null
        return (7 - days).coerceAtLeast(0)
    }

    // ── Study Readiness Score (0–100 per course) ──────────────────────────────
    /**
     * How prepared is the student for this course?
     *
     * Base = average progress across all course tasks.
     * Penalty for overdue tasks and low progress near an exam.
     *
     * Bands: Ready ≥ 70 · At Risk ≥ 40 · Danger < 40
     */
    fun studyReadinessScore(tasks: List<StudyTask>, course: Course): Int {
        val courseTasks = tasks.filter { it.courseId == course.id }
        if (courseTasks.isEmpty()) return 100

        val avgProgress = courseTasks.map { it.progress }.average()

        val overdueCount   = courseTasks.count { it.isOverdue() && !it.isCompleted }
        val overduePenalty = (overdueCount * 15).coerceAtMost(40).toDouble()

        val examPenalty = course.examDate?.let { examMillis ->
            val days = ((examMillis - System.currentTimeMillis()) / (1000L * 60 * 60 * 24)).toInt()
            if (days < 0) return@let 0.0          // exam already passed
            val deficit = ((70.0 - avgProgress) / 70.0).coerceAtLeast(0.0)
            when {
                days <= 3  -> 25.0 * deficit
                days <= 7  -> 15.0 * deficit
                days <= 14 ->  6.0 * deficit
                else       ->  0.0
            }
        } ?: 0.0

        return (avgProgress - overduePenalty - examPenalty).coerceIn(0.0, 100.0).toInt()
    }

    fun readinessLabel(score: Int): String = when {
        score >= 70 -> "Ready"
        score >= 40 -> "At Risk"
        else        -> "Danger"
    }

    // ── Score factor breakdown (for TaskDetailScreen) ─────────────────────────
    data class ScoreFactor(val label: String, val contribution: Double, val maxContribution: Double)

    fun scoreBreakdown(task: StudyTask): List<ScoreFactor> {
        val urgencyNorm    = task.urgency / 5.0
        val gradeImpact    = task.gradeWeight.toDouble()
        val difficultyNorm = task.difficulty / 5.0
        val lowProgress    = (100 - task.progress) / 100.0
        val deadline       = deadlineScore(task)
        val overload       = urgencyNorm
        return listOf(
            ScoreFactor("Urgency",       0.30 * urgencyNorm    * 100, 30.0),
            ScoreFactor("Grade Impact",  0.25 * gradeImpact    * 100, 25.0),
            ScoreFactor("Difficulty",    0.15 * difficultyNorm * 100, 15.0),
            ScoreFactor("Low Progress",  0.10 * lowProgress    * 100, 10.0),
            ScoreFactor("Deadline",      0.10 * deadline       * 100, 10.0),
            ScoreFactor("Overload",      0.10 * overload       * 100, 10.0)
        )
    }

    // ── Local advice ──────────────────────────────────────────────────────────
    fun getAdvice(tasks: List<StudyTask>): String {
        val active = tasks.filter { !it.isCompleted }
        if (tasks.isEmpty()) return "Add tasks to get started"
        if (active.isEmpty()) return "All tasks complete — great work!"

        val overdue   = active.overdueTasks()
        val critical  = active.filter { score(it) >= 80 }
        val top       = sortByPriority(active).firstOrNull()
        val busiest   = subjectBreakdown(active).entries.firstOrNull()

        return when {
            overdue.isNotEmpty() -> {
                val worst = overdue.maxByOrNull { score(it) }
                "${overdue.size} overdue task(s)! Start with \"${worst?.title}\""
            }
            critical.isNotEmpty() -> {
                val worst = critical.maxByOrNull { score(it) }
                "${critical.size} critical task(s) — tackle \"${worst?.title}\" first"
            }
            busiest != null && subjectBreakdown(tasks).size > 1 -> {
                "\"${busiest.key}\" needs the most attention today"
            }
            top != null -> {
                "Start with \"${top.title}\" — score ${String.format("%.0f", score(top))}/100"
            }
            else -> "All clear — great balance!"
        }
    }
}
