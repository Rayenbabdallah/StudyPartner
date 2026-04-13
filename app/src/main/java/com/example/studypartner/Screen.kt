package com.example.studypartner

sealed class Screen(val route: String) {
    // Entry and auth
    object Splash : Screen("splash")
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object EmailVerification : Screen("email_verification")

    // Core bottom tabs
    object Home : Screen("dashboard")
    object Planner : Screen("planner")
    object List : Screen("tasks")
    object Courses : Screen("courses")
    object AiAssistant : Screen("ai_assistant")

    // Dashboard sub-screens
    object DashboardDetails : Screen("dashboard_details")
    object FullRiskAlerts : Screen("full_risk_alerts")
    object FullWeeklySummary : Screen("full_weekly_summary")

    // Task module
    object Add : Screen("add_task")
    object TodayTasks : Screen("today_tasks")
    object UpcomingTasks : Screen("upcoming_tasks")
    object OverdueTasks : Screen("overdue_tasks")
    object CompletedTasks : Screen("completed_tasks")
    object Subtasks : Screen("subtasks")
    object FilterTasks : Screen("filter_tasks")

    // Courses module
    object AddCourse : Screen("add_course")
    object EditCourse : Screen("edit_course/{courseId}") {
        fun createRoute(courseId: Int) = "edit_course/$courseId"
    }
    object CourseAssignments : Screen("course_assignments/{courseId}") {
        fun createRoute(courseId: Int) = "course_assignments/$courseId"
    }
    object CourseProgress : Screen("course_progress/{courseId}") {
        fun createRoute(courseId: Int) = "course_progress/$courseId"
    }
    object CourseExamInfo : Screen("course_exam_info/{courseId}") {
        fun createRoute(courseId: Int) = "course_exam_info/$courseId"
    }
    object CourseStudySessions : Screen("course_study_sessions/{courseId}") {
        fun createRoute(courseId: Int) = "course_study_sessions/$courseId"
    }

    // Planner module
    object PlannerDaily : Screen("planner_daily/{dateMillis}") {
        fun createRoute(dateMillis: Long) = "planner_daily/$dateMillis"
    }
    object PlannerWeekly : Screen("planner_weekly")
    object StudySessionDetail : Screen("study_session_detail/{sessionId}") {
        fun createRoute(sessionId: Int) = "study_session_detail/$sessionId"
    }
    object AddStudySession : Screen("add_study_session?taskId={taskId}&courseId={courseId}") {
        private const val BASE_ROUTE = "add_study_session"

        fun createRoute(taskId: Int? = null, courseId: Int? = null): String {
            val params = mutableListOf<String>()
            if (taskId != null) params += "taskId=$taskId"
            if (courseId != null) params += "courseId=$courseId"
            return if (params.isEmpty()) BASE_ROUTE else "$BASE_ROUTE?${params.joinToString("&")}"
        }
    }
    object EditStudySession : Screen("edit_study_session/{sessionId}") {
        fun createRoute(sessionId: Int) = "edit_study_session/$sessionId"
    }
    object OverloadVisualization : Screen("overload_visualization")

    // AI assistant module
    object AiPrompts : Screen("ai_prompts")
    object AiGeneratedPlan : Screen("ai_generated_plan")
    object AiTaskBreakdown : Screen("ai_task_breakdown/{taskId}") {
        fun createRoute(taskId: Int) = "ai_task_breakdown/$taskId"
    }
    object RecoveryPlan : Screen("recovery_plan")

    // Risk and insights module
    object RiskInsights : Screen("risk_insights")
    object CourseRiskBreakdown : Screen("course_risk_breakdown")
    object DeadlineRiskDetails : Screen("deadline_risk_details")
    object OverloadAnalysis : Screen("overload_analysis")
    object StudyReadiness : Screen("study_readiness")

    // Analytics module
    object Stats : Screen("stats")
    object WeeklyReview : Screen("weekly_review")
    object StudyTimeAnalytics : Screen("study_time_analytics")
    object TaskCompletionStats : Screen("task_completion_stats")
    object CoursePerformance : Screen("course_performance")

    // Notifications, search, profile and settings
    object Notifications : Screen("notifications")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object AccountSettings : Screen("account_settings")
    object NotificationSettings : Screen("notification_settings")
    object AppPreferences : Screen("app_preferences")
    object ThemeSettings : Screen("theme_settings")
    object LanguageTone : Screen("language_tone")
    object GlobalSearch : Screen("global_search")

    // Ghasret Lekleb mode
    object GhasretActivation : Screen("ghasret_activation")
    object GhasretLekleb : Screen("ghasret_lekleb")
    object SaveMePlan : Screen("save_me_plan")
    object TopPriorityTask : Screen("top_priority_task")
    object DoNowTasks : Screen("do_now_tasks")
    object SkipThese : Screen("skip_these")
    object TimeBasedRescue : Screen("time_based_rescue")
    object RescuePlanDetail : Screen("rescue_plan_detail")
    object QuickFocusMode : Screen("quick_focus_mode")

    // Focus mode
    object Edit : Screen("edit/{taskId}") {
        fun createRoute(taskId: Int) = "edit/$taskId"
    }
    object TaskDetail : Screen("task_detail/{taskId}") {
        fun createRoute(taskId: Int) = "task_detail/$taskId"
    }
    object CourseDetail : Screen("course_detail/{courseId}") {
        fun createRoute(courseId: Int) = "course_detail/$courseId"
    }
    object Focus : Screen("focus/{taskId}") {
        fun createRoute(taskId: Int) = "focus/$taskId"
    }
    object TimerCountdown : Screen("timer_countdown/{taskId}") {
        fun createRoute(taskId: Int) = "timer_countdown/$taskId"
    }
    object SessionCompletion : Screen("session_completion/{taskId}") {
        fun createRoute(taskId: Int) = "session_completion/$taskId"
    }

    // System UX screens
    object EmptyStateGallery : Screen("empty_state_gallery")
    object LoadingStateGallery : Screen("loading_state_gallery")
    object ErrorStateGallery : Screen("error_state_gallery")
    object ConfirmationsDemo : Screen("confirmations_demo")
    object SuccessFeedbackDemo : Screen("success_feedback_demo")

    // Hidden/system indicators
    object SyncStatus : Screen("sync_status")
    object OfflineBannerDemo : Screen("offline_banner_demo")
    object DataConflictResolve : Screen("data_conflict_resolve")
}
