package com.example.studypartner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val DAY_MS = 1000L * 60 * 60 * 24

private fun baseTask(
    difficulty: Int = 2,
    urgency: Int = 2,
    progress: Int = 0,
    deadline: Long? = null,
) = StudyTask(
    title = "t",
    subject = "s",
    difficulty = difficulty,
    urgency = urgency,
    deadline = deadline,
    progress = progress,
)

class StudyTaskExtensionsTest {

    @Test
    fun isCompleted_trueAtOrAbove100() {
        assertTrue(baseTask(progress = 100).isCompleted)
        assertFalse(baseTask(progress = 99).isCompleted)
    }

    @Test
    fun priority_sumsDifficultyAndUrgency() {
        assertEquals(7, baseTask(difficulty = 3, urgency = 4).priority())
    }

    @Test
    fun isHighRisk_requiresBothFactorsAtLeast4() {
        assertTrue(baseTask(difficulty = 4, urgency = 4).isHighRisk())
        assertFalse(baseTask(difficulty = 5, urgency = 3).isHighRisk())
    }

    @Test
    fun riskLabel_bandsMatchThresholds() {
        assertEquals("High Risk", baseTask(difficulty = 4, urgency = 5).riskLabel())
        assertEquals("Moderate",  baseTask(difficulty = 3, urgency = 1).riskLabel())
        assertEquals("Safe",      baseTask(difficulty = 1, urgency = 2).riskLabel())
    }

    @Test
    fun daysUntilDeadline_nullWhenNoDeadline() {
        assertNull(baseTask().daysUntilDeadline())
    }

    @Test
    fun isOverdue_trueForPastDeadline() {
        val overdue = baseTask(deadline = System.currentTimeMillis() - 2 * DAY_MS)
        assertTrue(overdue.isOverdue())
    }

    @Test
    fun deadlineLabel_formatsPastPresentFuture() {
        val overdue = baseTask(deadline = System.currentTimeMillis() - 2 * DAY_MS)
        val today   = baseTask(deadline = System.currentTimeMillis() + DAY_MS / 4)
        val future  = baseTask(deadline = System.currentTimeMillis() + 3 * DAY_MS + DAY_MS / 2)
        assertTrue(overdue.deadlineLabel().startsWith("Overdue"))
        assertEquals("Due today!", today.deadlineLabel())
        assertTrue(future.deadlineLabel().startsWith("Due in"))
    }

    @Test
    fun withProgress_clampsTo0And100() {
        assertEquals(0,   baseTask().withProgress(-5).progress)
        assertEquals(100, baseTask().withProgress(250).progress)
        assertEquals(42,  baseTask().withProgress(42).progress)
    }

    @Test
    fun toggleCompleted_flipsBetween0And100() {
        val t = baseTask(progress = 40)
        assertEquals(100, t.toggleCompleted().progress)
        assertEquals(0,   t.copy(progress = 100).toggleCompleted().progress)
    }

    @Test
    fun markAsUrgent_setsExtremeLevel() {
        assertEquals(Level.EXTREME.value, baseTask(urgency = 1).markAsUrgent().urgency)
    }

    @Test
    fun listHelpers_filterCorrectly() {
        val done    = baseTask(progress = 100)
        val overdue = baseTask(deadline = System.currentTimeMillis() - DAY_MS)
        val risky   = baseTask(difficulty = 5, urgency = 5)
        val all     = listOf(done, overdue, risky)
        assertEquals(listOf(overdue, risky), all.activeTasks())
        assertEquals(listOf(overdue),        all.overdueTasks())
        assertEquals(listOf(risky),          all.highRiskTasks())
        assertEquals(risky,                  all.topTask())
    }
}
