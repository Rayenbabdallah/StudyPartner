package com.example.studypartner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val DAY_MS = 1000L * 60 * 60 * 24

private fun task(
    id: Int = 0,
    title: String = "t",
    subject: String = "Math",
    courseId: Int? = null,
    difficulty: Int = 3,
    urgency: Int = 3,
    daysFromNow: Int? = 7,
    type: String = TaskType.ASSIGNMENT.name,
    gradeWeight: Float = GradeImpact.MEDIUM.weight,
    progress: Int = 0,
) = StudyTask(
    id = id,
    title = title,
    subject = subject,
    courseId = courseId,
    difficulty = difficulty,
    urgency = urgency,
    deadline = daysFromNow?.let { System.currentTimeMillis() + it * DAY_MS + DAY_MS / 2 },
    type = type,
    gradeWeight = gradeWeight,
    progress = progress,
)

class PriorityEngineTest {

    @Test
    fun score_isZero_forMinimalTask() {
        val t = task(difficulty = 0, urgency = 0, gradeWeight = 0f, progress = 100, daysFromNow = null)
        assertEquals(0.0, PriorityEngine.score(t), 0.001)
    }

    @Test
    fun score_isHundred_forMaxedTask() {
        val t = task(difficulty = 5, urgency = 5, gradeWeight = 1f, progress = 0, daysFromNow = 0)
        assertEquals(100.0, PriorityEngine.score(t), 0.001)
    }

    @Test
    fun score_increases_whenDeadlineCloser() {
        val far  = task(daysFromNow = 60)
        val near = task(daysFromNow = 1)
        assertTrue(PriorityEngine.score(near) > PriorityEngine.score(far))
    }

    @Test
    fun scoreBreakdown_sumsToTotalScore() {
        val t = task(difficulty = 4, urgency = 5, gradeWeight = 0.8f, progress = 20, daysFromNow = 2)
        val breakdownSum = PriorityEngine.scoreBreakdown(t).sumOf { it.contribution }
        assertEquals(PriorityEngine.score(t), breakdownSum, 0.001)
    }

    @Test
    fun scoreBreakdown_maxContributionsSumToHundred() {
        val max = PriorityEngine.scoreBreakdown(task()).sumOf { it.maxContribution }
        assertEquals(100.0, max, 0.001)
    }

    @Test
    fun sortByPriority_putsCompletedLast_andHighestFirst() {
        val critical = task(id = 1, difficulty = 5, urgency = 5, gradeWeight = 1f, daysFromNow = 0)
        val low      = task(id = 2, difficulty = 1, urgency = 1, gradeWeight = 0.1f, daysFromNow = 60)
        val done     = task(id = 3, difficulty = 5, urgency = 5, gradeWeight = 1f, progress = 100)
        val sorted = PriorityEngine.sortByPriority(listOf(low, done, critical))
        assertEquals(listOf(1, 2, 3), sorted.map { it.id })
    }

    @Test
    fun studyLoad_ignoresCompleted() {
        val active   = task(difficulty = 3, urgency = 4) // priority 7
        val finished = task(difficulty = 5, urgency = 5, progress = 100)
        assertEquals(7, PriorityEngine.studyLoad(listOf(active, finished)))
    }

    @Test
    fun recommendedSessionMinutes_matchesBands() {
        assertEquals(0,   PriorityEngine.recommendedSessionMinutes(emptyList()))
        assertEquals(30,  PriorityEngine.recommendedSessionMinutes(listOf(task(difficulty = 2, urgency = 2))))
        assertEquals(60,  PriorityEngine.recommendedSessionMinutes(listOf(task(difficulty = 5, urgency = 5))))
        assertEquals(120, PriorityEngine.recommendedSessionMinutes(
            List(5) { task(difficulty = 5, urgency = 5) }
        ))
    }

    @Test
    fun subjectBreakdown_sortsDescending_andSkipsBlankSubjects() {
        val math1   = task(subject = "Math",    difficulty = 5, urgency = 5, gradeWeight = 1f, daysFromNow = 0)
        val math2   = task(subject = "Math",    difficulty = 2, urgency = 2)
        val physics = task(subject = "Physics", difficulty = 1, urgency = 1, gradeWeight = 0.1f, daysFromNow = 60)
        val blank   = task(subject = "",        difficulty = 5, urgency = 5)
        val result  = PriorityEngine.subjectBreakdown(listOf(math1, math2, physics, blank))
        assertEquals(listOf("Math", "Physics"), result.keys.toList())
        assertFalse(result.containsKey(""))
    }

    @Test
    fun isPanicMode_triggerA_singleHighStakesTask() {
        val panic = task(daysFromNow = 1, progress = 20, gradeWeight = 0.7f)
        assertTrue(PriorityEngine.isPanicMode(listOf(panic)))
    }

    @Test
    fun isPanicMode_triggerB_threeNearDeadlineTasks() {
        val three = List(3) { task(daysFromNow = 2, progress = 30, gradeWeight = 0.2f) }
        assertTrue(PriorityEngine.isPanicMode(three))
    }

    @Test
    fun isPanicMode_falseWhenNoTrigger() {
        val calm = listOf(task(daysFromNow = 30, progress = 80, gradeWeight = 0.2f))
        assertFalse(PriorityEngine.isPanicMode(calm))
    }

    @Test
    fun survivalScore_favorsHighGradeLowDifficultyNearDeadline() {
        val easyHigh = task(difficulty = 1, gradeWeight = 1f, daysFromNow = 0)
        val hardLow  = task(difficulty = 5, gradeWeight = 0.2f, daysFromNow = 30)
        assertTrue(PriorityEngine.survivalScore(easyHigh) > PriorityEngine.survivalScore(hardLow))
    }

    @Test
    fun sortBySurvival_excludesCompletedTasks() {
        val active = task(id = 1)
        val done   = task(id = 2, progress = 100)
        val sorted = PriorityEngine.sortBySurvival(listOf(active, done))
        assertEquals(listOf(1), sorted.map { it.id })
    }

    @Test
    fun riskOnsetDays_nullWhenNoDeadlineOrFar() {
        assertNull(PriorityEngine.riskOnsetDays(task(daysFromNow = null)))
        assertNull(PriorityEngine.riskOnsetDays(task(daysFromNow = 14)))
    }

    @Test
    fun riskOnsetDays_growsAsDeadlineApproaches() {
        val sevenOut = PriorityEngine.riskOnsetDays(task(daysFromNow = 7))
        val today    = PriorityEngine.riskOnsetDays(task(daysFromNow = 0))
        assertEquals(0, sevenOut)
        assertEquals(7, today)
    }

    @Test
    fun studyReadinessScore_emptyCourseIsFullyReady() {
        val course = Course(id = 1, title = "Bio")
        assertEquals(100, PriorityEngine.studyReadinessScore(emptyList(), course))
    }

    @Test
    fun studyReadinessScore_penalizesOverdueTasks() {
        val course = Course(id = 1, title = "Bio")
        val onTrack  = task(courseId = 1, progress = 80, daysFromNow = 30)
        val overdue  = task(courseId = 1, progress = 10, daysFromNow = -2)
        val base     = PriorityEngine.studyReadinessScore(listOf(onTrack), course)
        val withRisk = PriorityEngine.studyReadinessScore(listOf(onTrack, overdue), course)
        assertTrue(withRisk < base)
    }

    @Test
    fun readinessLabel_bandsMapCorrectly() {
        assertEquals("Ready",   PriorityEngine.readinessLabel(85))
        assertEquals("At Risk", PriorityEngine.readinessLabel(55))
        assertEquals("Danger",  PriorityEngine.readinessLabel(20))
    }

    @Test
    fun getAdvice_handlesEmpty_andAllComplete() {
        assertEquals("Add tasks to get started", PriorityEngine.getAdvice(emptyList()))
        val done = task(progress = 100)
        assertEquals("All tasks complete — great work!", PriorityEngine.getAdvice(listOf(done)))
    }

    @Test
    fun getAdvice_mentionsOverdueTaskByTitle() {
        val overdue = task(title = "Essay", daysFromNow = -1)
        val msg = PriorityEngine.getAdvice(listOf(overdue))
        assertTrue(msg.contains("Essay"))
        assertTrue(msg.contains("overdue"))
    }
}
