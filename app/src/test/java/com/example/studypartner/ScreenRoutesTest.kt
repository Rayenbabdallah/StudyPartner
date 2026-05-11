package com.example.studypartner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Static checks on the navigation graph defined in [Screen].
 *
 * These run on the JVM (no Robolectric / device needed). They protect against:
 *   - duplicate route strings
 *   - parameterised-route helpers producing malformed paths
 *   - tab routes drifting from the constants navigation depends on
 */
class ScreenRoutesTest {

    private val allRoutes: List<String> = listOf(
        Screen.Splash.route,
        Screen.Onboarding.route,
        Screen.Login.route,
        Screen.Register.route,
        Screen.ForgotPassword.route,
        Screen.EmailVerification.route,
        Screen.Home.route,
        Screen.Planner.route,
        Screen.List.route,
        Screen.Courses.route,
        Screen.AiAssistant.route,
        Screen.DashboardDetails.route,
        Screen.FullRiskAlerts.route,
        Screen.FullWeeklySummary.route,
        Screen.Add.route,
        Screen.TodayTasks.route,
        Screen.UpcomingTasks.route,
        Screen.OverdueTasks.route,
        Screen.CompletedTasks.route,
        Screen.Subtasks.route,
        Screen.FilterTasks.route,
        Screen.AddCourse.route,
        Screen.EditCourse.route,
        Screen.CourseAssignments.route,
        Screen.CourseProgress.route,
        Screen.CourseExamInfo.route,
        Screen.CourseStudySessions.route,
        Screen.PlannerDaily.route,
        Screen.PlannerWeekly.route,
        Screen.StudySessionDetail.route,
        Screen.AddStudySession.route,
        Screen.EditStudySession.route,
        Screen.OverloadVisualization.route,
        Screen.AiPrompts.route,
        Screen.AiGeneratedPlan.route,
        Screen.AiTaskBreakdown.route,
        Screen.RecoveryPlan.route,
        Screen.RiskInsights.route,
        Screen.CourseRiskBreakdown.route,
        Screen.DeadlineRiskDetails.route,
        Screen.OverloadAnalysis.route,
        Screen.StudyReadiness.route,
        Screen.Stats.route,
        Screen.WeeklyReview.route,
        Screen.StudyTimeAnalytics.route,
        Screen.TaskCompletionStats.route,
        Screen.CoursePerformance.route,
        Screen.Notifications.route,
        Screen.Profile.route,
        Screen.Settings.route,
        Screen.GhasretActivation.route,
        Screen.GhasretLekleb.route,
        Screen.QuickFocusMode.route,
        Screen.Edit.route,
        Screen.TaskDetail.route,
        Screen.CourseDetail.route,
        Screen.Focus.route,
    )

    @Test
    fun routesAreUnique() {
        val duplicates = allRoutes.groupingBy { it }.eachCount().filter { it.value > 1 }
        assertTrue(
            "Duplicate routes detected: ${duplicates.keys}",
            duplicates.isEmpty()
        )
    }

    @Test
    fun routesAreNonBlank() {
        allRoutes.forEach { route ->
            assertTrue("Route is blank: $route", route.isNotBlank())
        }
    }

    @Test
    fun parameterisedRoutes_declarePlaceholder() {
        // Templates registered in NavHost must contain "{x}" so navigation can match.
        val parameterised = listOf(
            Screen.EditCourse.route,
            Screen.CourseAssignments.route,
            Screen.PlannerDaily.route,
            Screen.AiTaskBreakdown.route,
            Screen.Edit.route,
            Screen.TaskDetail.route,
            Screen.CourseDetail.route,
            Screen.Focus.route,
        )
        parameterised.forEach { route ->
            assertTrue("Route '$route' missing {param} placeholder", route.contains("{"))
            assertTrue("Route '$route' missing closing }", route.contains("}"))
        }
    }

    @Test
    fun editCourse_createRoute_substitutesId() {
        assertEquals("edit_course/42", Screen.EditCourse.createRoute(42))
        assertEquals("edit_course/0",  Screen.EditCourse.createRoute(0))
    }

    @Test
    fun taskDetail_createRoute_substitutesId() {
        assertEquals("task_detail/7", Screen.TaskDetail.createRoute(7))
        assertNotEquals(Screen.TaskDetail.route, Screen.TaskDetail.createRoute(7))
    }

    @Test
    fun focus_createRoute_substitutesId() {
        assertEquals("focus/3", Screen.Focus.createRoute(3))
    }

    @Test
    fun addStudySession_createRoute_handlesAllParamCombinations() {
        // No params → bare base
        assertEquals("add_study_session", Screen.AddStudySession.createRoute())
        // taskId only
        assertEquals("add_study_session?taskId=5", Screen.AddStudySession.createRoute(taskId = 5))
        // courseId only
        assertEquals("add_study_session?courseId=9", Screen.AddStudySession.createRoute(courseId = 9))
        // both
        val both = Screen.AddStudySession.createRoute(taskId = 5, courseId = 9)
        assertTrue(both.startsWith("add_study_session?"))
        assertTrue(both.contains("taskId=5"))
        assertTrue(both.contains("courseId=9"))
    }

    @Test
    fun bottomTabRoutes_areStable() {
        // These five string literals are referenced from MainActivity's bottom-bar wiring.
        // If the constant changes, the bottom bar selection breaks silently.
        assertEquals("dashboard",    Screen.Home.route)
        assertEquals("tasks",        Screen.List.route)
        assertEquals("courses",      Screen.Courses.route)
        assertEquals("ai_assistant", Screen.AiAssistant.route)
        assertEquals("settings",     Screen.Settings.route)
    }

    @Test
    fun courseDetail_createRoute_isReversibleToBaseTemplate() {
        val base   = Screen.CourseDetail.route                 // "course_detail/{courseId}"
        val actual = Screen.CourseDetail.createRoute(11)       // "course_detail/11"
        assertEquals(base.substringBefore("/"), actual.substringBefore("/"))
    }
}
