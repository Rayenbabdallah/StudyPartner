package com.example.studypartner

object PriorityEngine {

    fun getAdvice(tasks: List<StudyTask>): String {
        val overdue  = tasks.overdueTasks()
        val highRisk = tasks.highRiskTasks()

        return when {
            tasks.isEmpty()       -> "Add tasks to get advice"
            overdue.isNotEmpty()  -> "${overdue.size} overdue task(s)! Handle them now"
            highRisk.isNotEmpty() -> "${highRisk.size} high-risk task(s) — focus there first"
            else                  -> tasks.topTask()
                                         ?.let { "Start with ${it.subject} (priority ${it.priority()})" }
                                         ?: "All clear — good balance!"
        }
    }

    fun sortByPriority(tasks: List<StudyTask>): List<StudyTask> =
        tasks.sortedWith(compareByDescending<StudyTask> { it.isOverdue() }
            .thenByDescending { it.priority() })

    fun studyLoad(tasks: List<StudyTask>): Int = tasks.sumOf { it.priority() }
}
