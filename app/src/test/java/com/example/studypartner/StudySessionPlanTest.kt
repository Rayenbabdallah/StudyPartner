package com.example.studypartner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StudySessionPlanTest {

    @Test
    fun defaults_matchBalancedGeneralSession() {
        val plan = StudySessionPlan()
        assertEquals(SessionContextType.GENERAL, plan.contextType)
        assertEquals("General Study", plan.contextLabel)
        assertNull(plan.taskId)
        assertNull(plan.courseId)
        assertEquals(25, plan.durationMinutes)
        assertEquals(25, plan.breakEveryMinutes)
        assertEquals(5,  plan.breakLengthMinutes)
    }

    @Test
    fun copy_preservesUntouchedFields() {
        val original = StudySessionPlan(durationMinutes = 60, breakEveryMinutes = 25)
        val updated  = original.copy(breakLengthMinutes = 10)
        assertEquals(60, updated.durationMinutes)
        assertEquals(25, updated.breakEveryMinutes)
        assertEquals(10, updated.breakLengthMinutes)
    }
}

class AiStateTest {

    @Test
    fun idleAndLoading_areSingletons() {
        assertEquals(AiState.Idle, AiState.Idle)
        assertEquals(AiState.Loading, AiState.Loading)
        assertEquals(AiState.Unavailable, AiState.Unavailable)
    }

    @Test
    fun success_equalityByResponse() {
        assertEquals(AiState.Success("ok"), AiState.Success("ok"))
    }

    @Test
    fun failure_equalityByMessage() {
        assertEquals(AiState.Failure("boom"), AiState.Failure("boom"))
    }
}

class TaskUiStateTest {

    @Test
    fun success_wrapsTaskList() {
        val tasks = listOf(StudyTask(title = "x", subject = "y", difficulty = 1, urgency = 1))
        val state = TaskUiState.Success(tasks)
        assertEquals(tasks, state.tasks)
    }

    @Test
    fun error_preservesMessage() {
        assertEquals("boom", TaskUiState.Error("boom").message)
    }
}
