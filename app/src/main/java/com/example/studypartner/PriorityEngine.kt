package com.example.studypartner

object PriorityEngine {

    fun getAdvice(tasks: List<StudyTask>): String {
        val highRisk = tasks.highRiskTasks()

        return when {
            tasks.isEmpty()       -> "Add tasks to get advice"
            highRisk.isNotEmpty() -> "You have ${highRisk.size} high-risk task(s). Focus on them first"
            else                  -> tasks.topTask()
                                         ?.let { "Start with ${it.subject} (priority ${it.priority()})" }
                                         ?: "Good balance"
        }
    }

    fun sortByPriority(tasks: List<StudyTask>): List<StudyTask> =
        tasks.sortedByDescending { it.priority() }

    fun studyLoad(tasks: List<StudyTask>): Int =
        tasks.sumOf { it.priority() }
}
