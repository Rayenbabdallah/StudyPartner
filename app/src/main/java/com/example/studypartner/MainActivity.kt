package com.example.studypartner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.studypartner.ui.theme.StudyPartnerTheme
import java.util.concurrent.TimeUnit

private const val ANIM_DURATION = 320

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

private val bottomNavItems = listOf(
    BottomNavItem("Dashboard", Icons.Default.Home, Screen.Home.route),
    BottomNavItem("Planner", Icons.Default.CalendarMonth, Screen.Planner.route),
    BottomNavItem("Tasks", Icons.Default.FormatListBulleted, Screen.List.route),
    BottomNavItem("Courses", Icons.Default.School, Screen.Courses.route),
    BottomNavItem("AI", Icons.Default.AutoAwesome, Screen.AiAssistant.route),
)

private val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()

class MainActivity : ComponentActivity() {

    private val viewModel: StudyViewModel by viewModels()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "deadline_check",
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<NotificationWorker>(1, TimeUnit.DAYS).build()
        )

        setContent {
            val themeMode by UserPreferences.themeMode(this).collectAsState(initial = UserPreferences.THEME_SYSTEM)
            val darkTheme = when (themeMode) {
                UserPreferences.THEME_DARK -> true
                UserPreferences.THEME_LIGHT -> false
                else -> isSystemInDarkTheme()
            }

            StudyPartnerTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val onboardingDone by UserPreferences.onboardingDone(this).collectAsState(initial = null)
                val loggedIn by UserPreferences.loggedIn(this).collectAsState(initial = null)
                val emailVerified by UserPreferences.emailVerified(this).collectAsState(initial = null)
                val tunisianMode by UserPreferences.tunisianMode(this).collectAsState(initial = false)

                if (onboardingDone == null || loggedIn == null || emailVerified == null) return@StudyPartnerTheme
                val splashTargetRoute = when {
                    onboardingDone != true -> Screen.Onboarding.route
                    loggedIn == true && emailVerified == true -> Screen.Home.route
                    loggedIn == true -> Screen.EmailVerification.route
                    else -> Screen.Login.route
                }

                val navBackStack by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStack?.destination?.route
                val showBottomNav = currentRoute in bottomNavRoutes

                CompositionLocalProvider(LocalTunisianMode provides tunisianMode) {
                    Scaffold(
                        bottomBar = {
                            AnimatedVisibility(
                                visible = showBottomNav,
                                enter = slideInVertically(tween(260)) { it } + fadeIn(tween(200)),
                                exit = slideOutVertically(tween(260)) { it } + fadeOut(tween(200))
                            ) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    tonalElevation = 0.dp
                                ) {
                                    bottomNavItems.forEach { item ->
                                        NavigationBarItem(
                                            selected = currentRoute == item.route,
                                            onClick = { navController.navigateBottomNav(item.route) },
                                            icon = { Icon(item.icon, contentDescription = null) },
                                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Screen.Splash.route,
                            enterTransition = {
                                slideInHorizontally(tween(ANIM_DURATION)) { it } + fadeIn(tween(ANIM_DURATION))
                            },
                            exitTransition = {
                                slideOutHorizontally(tween(ANIM_DURATION)) { -it / 3 } + fadeOut(tween(ANIM_DURATION))
                            },
                            popEnterTransition = {
                                slideInHorizontally(tween(ANIM_DURATION)) { -it } + fadeIn(tween(ANIM_DURATION))
                            },
                            popExitTransition = {
                                slideOutHorizontally(tween(ANIM_DURATION)) { it } + fadeOut(tween(ANIM_DURATION))
                            }
                        ) {
                            composable(Screen.Splash.route) {
                                SplashPlaceholderScreen(navController, splashTargetRoute)
                            }
                            composable(Screen.Onboarding.route) { OnboardingScreen(navController) }
                            composable(Screen.Login.route) { LoginScreen(navController) }
                            composable(Screen.Register.route) { RegisterScreen(navController) }
                            composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
                            composable(Screen.EmailVerification.route) { EmailVerificationScreen(navController) }

                            composable(Screen.Home.route) { HomeScreen(navController, viewModel, innerPadding) }
                            composable(Screen.Planner.route) { PlannerScreen(navController, viewModel, innerPadding) }
                            composable(Screen.List.route) { TaskListScreen(navController, viewModel, innerPadding) }
                            composable(Screen.Courses.route) { CoursesScreen(navController, viewModel, innerPadding) }
                            composable(Screen.AiAssistant.route) {
                                AiAssistantScreen(navController, viewModel, innerPadding)
                            }

                            composable(Screen.Add.route) { AddTaskScreen(navController, viewModel) }
                            composable(Screen.Stats.route) { StatisticsScreen(navController, viewModel, innerPadding) }
                            composable(Screen.Settings.route) { SettingsScreen(navController, innerPadding) }
                            composable(Screen.AddCourse.route) { AddCourseScreen(navController, viewModel) }

                            composable(Screen.RiskInsights.route) { RiskInsightsScreen(navController, viewModel) }
                            composable(Screen.FullRiskAlerts.route) { RiskInsightsScreen(navController, viewModel) }
                            composable(Screen.WeeklyReview.route) { WeeklyReviewScreen(navController, viewModel) }
                            composable(Screen.FullWeeklySummary.route) { WeeklyReviewScreen(navController, viewModel) }

                            composable(Screen.TodayTasks.route) {
                                TaskStateScreen(navController, viewModel, "Today Tasks") { task ->
                                    val days = task.daysUntilDeadline()
                                    !task.isCompleted && days != null && days in 0..1
                                }
                            }
                            composable(Screen.UpcomingTasks.route) {
                                TaskStateScreen(navController, viewModel, "Upcoming Tasks") { task ->
                                    val days = task.daysUntilDeadline()
                                    !task.isCompleted && days != null && days >= 2
                                }
                            }
                            composable(Screen.OverdueTasks.route) {
                                TaskStateScreen(navController, viewModel, "Overdue Tasks") { task ->
                                    !task.isCompleted && task.isOverdue()
                                }
                            }
                            composable(Screen.CompletedTasks.route) {
                                TaskStateScreen(navController, viewModel, "Completed Tasks") { task ->
                                    task.isCompleted
                                }
                            }
                            composable(Screen.Subtasks.route) {
                                SubtasksOverviewScreen(navController, viewModel)
                            }
                            composable(Screen.FilterTasks.route) {
                                FilterTasksScreen(navController, viewModel)
                            }

                            composable(Screen.Notifications.route) {
                                NotificationsCenterScreen(navController, viewModel, innerPadding)
                            }
                            composable(Screen.Profile.route) { ProfileScreen(navController, innerPadding) }
                            composable(Screen.AccountSettings.route) {
                                AccountSettingsScreen(navController)
                            }
                            composable(Screen.NotificationSettings.route) {
                                NotificationSettingsScreen(navController)
                            }
                            composable(Screen.AppPreferences.route) {
                                AppPreferencesScreen(navController)
                            }
                            composable(Screen.ThemeSettings.route) {
                                ThemeSettingsScreen(navController)
                            }
                            composable(Screen.LanguageTone.route) {
                                LanguageToneScreen(navController)
                            }
                            composable(Screen.GlobalSearch.route) {
                                GlobalSearchScreen(navController, viewModel)
                            }
                            composable(Screen.DashboardDetails.route) {
                                DashboardSubscreensHub(navController)
                            }

                            composable(Screen.AiPrompts.route) { AiPromptsScreen(navController, viewModel) }
                            composable(Screen.AiGeneratedPlan.route) { AiGeneratedPlanScreen(navController, viewModel) }
                            composable(Screen.RecoveryPlan.route) {
                                RecoveryPlanScreen(navController, viewModel)
                            }

                            composable(Screen.CourseRiskBreakdown.route) {
                                RiskInsightsScreen(navController, viewModel)
                            }
                            composable(Screen.DeadlineRiskDetails.route) {
                                TaskStateScreen(navController, viewModel, "Deadline Risk Details") { task ->
                                    !task.isCompleted && (task.isOverdue() || (task.daysUntilDeadline() ?: Int.MAX_VALUE) <= 2)
                                }
                            }
                            composable(Screen.OverloadAnalysis.route) {
                                OverloadAnalysisScreen(navController, viewModel)
                            }
                            composable(Screen.StudyReadiness.route) {
                                StudyReadinessSummaryScreen(navController, viewModel)
                            }

                            composable(Screen.StudyTimeAnalytics.route) {
                                StatisticsScreen(navController, viewModel, innerPadding)
                            }
                            composable(Screen.TaskCompletionStats.route) {
                                StatisticsScreen(navController, viewModel, innerPadding)
                            }
                            composable(Screen.CoursePerformance.route) {
                                RiskInsightsScreen(navController, viewModel)
                            }

                            composable(Screen.GhasretActivation.route) {
                                GhasretActivationScreen(navController, viewModel)
                            }
                            composable(Screen.GhasretLekleb.route) { GhasretLeklebScreen(navController, viewModel) }
                            composable(Screen.SaveMePlan.route) {
                                RecoveryPlanScreen(navController, viewModel)
                            }
                            composable(Screen.TopPriorityTask.route) {
                                TopPriorityTaskScreen(navController, viewModel)
                            }
                            composable(Screen.DoNowTasks.route) {
                                TaskStateScreen(navController, viewModel, "Do Now Tasks") { task ->
                                    !task.isCompleted && task.score() >= 70
                                }
                            }
                            composable(Screen.SkipThese.route) {
                                SkipTheseScreen(navController, viewModel)
                            }
                            composable(Screen.TimeBasedRescue.route) {
                                TimeBasedRescueScreen(navController, viewModel)
                            }
                            composable(Screen.RescuePlanDetail.route) {
                                TimeBasedRescueScreen(navController, viewModel)
                            }
                            composable(Screen.QuickFocusMode.route) {
                                QuickFocusTimerScreen(navController, viewModel)
                            }

                            composable(Screen.EmptyStateGallery.route) {
                                SystemGalleryScreen(navController, "Empty States", "No tasks, no courses, and no sessions variants.")
                            }
                            composable(Screen.LoadingStateGallery.route) {
                                SystemGalleryScreen(navController, "Loading States", "Skeleton and loading placeholders.")
                            }
                            composable(Screen.ErrorStateGallery.route) {
                                SystemGalleryScreen(navController, "Error States", "Network and retry surfaces.")
                            }
                            composable(Screen.ConfirmationsDemo.route) {
                                SystemGalleryScreen(navController, "Confirmation Dialogs", "Delete/complete confirmation patterns.")
                            }
                            composable(Screen.SuccessFeedbackDemo.route) {
                                SystemGalleryScreen(navController, "Success Feedback", "Completion and success banners.")
                            }
                            composable(Screen.SyncStatus.route) {
                                SyncStatusScreen(navController)
                            }
                            composable(Screen.OfflineBannerDemo.route) {
                                OfflineModeBannerScreen(navController)
                            }
                            composable(Screen.DataConflictResolve.route) {
                                DataConflictResolutionScreen(navController)
                            }

                            composable(
                                route = Screen.TaskDetail.route,
                                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                            ) { back ->
                                val taskId = back.arguments?.getInt("taskId") ?: return@composable
                                TaskDetailScreen(navController, viewModel, taskId)
                            }
                            composable(
                                route = Screen.CourseDetail.route,
                                arguments = listOf(navArgument("courseId") { type = NavType.IntType })
                            ) { back ->
                                val courseId = back.arguments?.getInt("courseId") ?: return@composable
                                CourseDetailScreen(navController, viewModel, courseId)
                            }
                            composable(
                                route = Screen.Edit.route,
                                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                            ) { back ->
                                val taskId = back.arguments?.getInt("taskId") ?: return@composable
                                EditTaskScreen(navController, viewModel, taskId)
                            }
                            composable(
                                route = Screen.EditCourse.route,
                                arguments = listOf(navArgument("courseId") { type = NavType.IntType })
                            ) { back ->
                                val courseId = back.arguments?.getInt("courseId") ?: return@composable
                                AddCourseScreen(navController, viewModel, existingCourseId = courseId)
                            }
                            composable(
                                route = Screen.Focus.route,
                                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                            ) { back ->
                                val taskId = back.arguments?.getInt("taskId") ?: return@composable
                                FocusScreen(navController, viewModel, taskId)
                            }
                            composable(
                                route = Screen.AiTaskBreakdown.route,
                                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                            ) { back ->
                                val taskId = back.arguments?.getInt("taskId") ?: return@composable
                                AiTaskBreakdownScreen(navController, viewModel, taskId)
                            }
                            composable(
                                route = Screen.StudySessionDetail.route,
                                arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
                            ) { back ->
                                val sessionId = back.arguments?.getInt("sessionId") ?: return@composable
                                StudySessionDetailScreen(navController, viewModel, sessionId)
                            }
                            composable(
                                route = Screen.EditStudySession.route,
                                arguments = listOf(navArgument("sessionId") { type = NavType.IntType })
                            ) { back ->
                                val sessionId = back.arguments?.getInt("sessionId") ?: return@composable
                                StudySessionFormScreen(
                                    navController = navController,
                                    viewModel = viewModel,
                                    title = "Edit Study Session",
                                    subtitle = "Edit session details.",
                                    existingSessionId = sessionId,
                                    defaultName = "Session $sessionId"
                                )
                            }
                            composable(
                                route = Screen.CourseAssignments.route,
                                arguments = listOf(navArgument("courseId") { type = NavType.IntType })
                            ) { back ->
                                val courseId = back.arguments?.getInt("courseId") ?: return@composable
                                CourseScopedScreen(
                                    navController = navController,
                                    viewModel = viewModel,
                                    courseId = courseId,
                                    title = "Course Assignments",
                                    subtitle = "Tasks filtered by selected course."
                                )
                            }
                            composable(
                                route = Screen.CourseProgress.route,
                                arguments = listOf(navArgument("courseId") { type = NavType.IntType })
                            ) { back ->
                                val courseId = back.arguments?.getInt("courseId") ?: return@composable
                                CourseScopedScreen(
                                    navController = navController,
                                    viewModel = viewModel,
                                    courseId = courseId,
                                    title = "Course Progress",
                                    subtitle = "Progress and readiness for the selected course."
                                )
                            }
                            composable(
                                route = Screen.CourseExamInfo.route,
                                arguments = listOf(navArgument("courseId") { type = NavType.IntType })
                            ) { back ->
                                val courseId = back.arguments?.getInt("courseId") ?: return@composable
                                CourseScopedScreen(
                                    navController = navController,
                                    viewModel = viewModel,
                                    courseId = courseId,
                                    title = "Exam Info",
                                    subtitle = "Exam-specific data for selected course."
                                )
                            }
                            composable(
                                route = Screen.CourseStudySessions.route,
                                arguments = listOf(navArgument("courseId") { type = NavType.IntType })
                            ) { back ->
                                val courseId = back.arguments?.getInt("courseId") ?: return@composable
                                CourseScopedScreen(
                                    navController = navController,
                                    viewModel = viewModel,
                                    courseId = courseId,
                                    title = "Course Study Sessions",
                                    subtitle = "Session history and planning for selected course."
                                )
                            }
                            composable(
                                route = Screen.PlannerDaily.route,
                                arguments = listOf(navArgument("dateMillis") { type = NavType.LongType })
                            ) { back ->
                                val dateMillis = back.arguments?.getLong("dateMillis") ?: return@composable
                                PlannerDailyScreen(navController, dateMillis)
                            }
                            composable(Screen.PlannerWeekly.route) {
                                PlannerWeeklyScreen(navController)
                            }
                            composable(
                                route = Screen.AddStudySession.route,
                                arguments = listOf(
                                    navArgument("taskId") {
                                        type = NavType.IntType
                                        defaultValue = -1
                                    },
                                    navArgument("courseId") {
                                        type = NavType.IntType
                                        defaultValue = -1
                                    }
                                )
                            ) { back ->
                                val taskId = back.arguments?.getInt("taskId")?.takeIf { it > 0 }
                                val courseId = back.arguments?.getInt("courseId")?.takeIf { it > 0 }
                                StudySessionFormScreen(
                                    navController = navController,
                                    viewModel = viewModel,
                                    title = "Add Study Session",
                                    subtitle = "Create a focused study session.",
                                    initialTaskId = taskId,
                                    initialCourseId = courseId
                                )
                            }
                            composable(Screen.OverloadVisualization.route) {
                                OverloadAnalysisScreen(navController, viewModel)
                            }
                            composable(
                                route = Screen.TimerCountdown.route,
                                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                            ) { back ->
                                val taskId = back.arguments?.getInt("taskId") ?: return@composable
                                TimerCountdownScreen(navController, viewModel, taskId)
                            }
                            composable(
                                route = Screen.SessionCompletion.route,
                                arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                            ) { back ->
                                val taskId = back.arguments?.getInt("taskId") ?: return@composable
                                SessionCompletionScreen(navController, viewModel, taskId)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun NavController.navigateBottomNav(route: String) {
    navigate(route) {
        popUpTo(Screen.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
