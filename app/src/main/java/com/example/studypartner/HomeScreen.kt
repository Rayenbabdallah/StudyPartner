package com.example.studypartner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    outerPadding: PaddingValues = PaddingValues()
) {
    val state       by viewModel.uiState.collectAsState()
    val ollamaState by viewModel.ollamaState.collectAsState()
    val appMode     by viewModel.appMode.collectAsState()
    val sessions    by viewModel.allStudySessions.collectAsState()
    var panicBannerDismissed by remember { mutableStateOf(false) }

    val localAdvice = when (state) {
        is TaskUiState.Loading -> "Analysing your tasks…"
        is TaskUiState.Success -> viewModel.getAdvice()
        is TaskUiState.Error   -> (state as TaskUiState.Error).message
    }
    val tasks = (state as? TaskUiState.Success)?.tasks ?: emptyList()
    val sortedActiveTasks = viewModel.sortedTasks().filter { !it.isCompleted }
    val bestNextTask = sortedActiveTasks.firstOrNull()
    val todayTasks = tasks.filter { task ->
        !task.isCompleted && (task.daysUntilDeadline() == 0)
    }
    val upcomingDeadlines = tasks
        .filter { !it.isCompleted }
        .filter { task ->
            val days = task.daysUntilDeadline()
            days != null && days in 1..7
        }
        .sortedBy { it.deadline ?: Long.MAX_VALUE }
        .take(5)
    val highRiskCount = tasks.count { !it.isCompleted && it.isHighRisk() }
    val overdueCount = tasks.count { !it.isCompleted && it.isOverdue() }
    val completionRate = if (tasks.isNotEmpty()) tasks.count { it.isCompleted }.toFloat() / tasks.size else 0f
    val avgProgress = if (tasks.isNotEmpty()) tasks.map { it.progress }.average().toInt() else 0
    val weekStartMillis = remember {
        val cal = java.util.Calendar.getInstance().apply {
            firstDayOfWeek = java.util.Calendar.MONDAY
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis
    }
    val weeklyStudyMinutes = sessions
        .filter { it.updatedAt >= weekStartMillis }
        .sumOf { it.durationMinutes }
    val weeklyStudyHours = weeklyStudyMinutes / 60f
    val studyGoalHours = 12f

    LaunchedEffect(tasks.size) {
        if (tasks.isNotEmpty() && ollamaState is AiState.Idle) {
            viewModel.fetchOllamaAdvice()
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "StudyPartner",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Your AI-powered study companion",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    Row {
                        IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                            Icon(Icons.Default.Warning, contentDescription = "Notifications")
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = { navController.navigate(Screen.AiAssistant.route) }) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant")
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor            = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor    = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor         = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor    = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.Add.route) },
                icon    = { Icon(Icons.Default.Add, contentDescription = null) },
                text    = { Text("Add Task", style = MaterialTheme.typography.labelLarge) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .padding(bottom = outerPadding.calculateBottomPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Panic banner ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = appMode == AppMode.GHASRET_LEKLEB && !panicBannerDismissed,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                PanicBanner(
                    onOpen    = { navController.navigate(Screen.GhasretActivation.route) },
                    onDismiss = { panicBannerDismissed = true; viewModel.dismissPanic() }
                )
            }

            // ── Stats row ─────────────────────────────────────────────────────
            StatsRow(
                taskCount  = tasks.size,
                atRisk     = tasks.highRiskTasks().size,
                completed  = tasks.count { it.isCompleted },
                sessionMin = viewModel.recommendedSession()
            )

            // ── AI Advice card ────────────────────────────────────────────────
            

            AdviceCard(
                localAdvice = localAdvice,
                ollamaState = ollamaState,
                onRefresh   = { viewModel.fetchOllamaAdvice() }
            )

            Text(
                "TODAY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (todayTasks.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Text(
                        "No tasks due today. Use this block for proactive study.",
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                todayTasks.take(3).forEach { task ->
                    ElevatedCard(onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(task.title, fontWeight = FontWeight.SemiBold)
                            Text(task.deadlineLabel(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Text(
                "BEST NEXT TASK",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (bestNextTask == null) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Text("All caught up. No active tasks.", modifier = Modifier.padding(14.dp))
                }
            } else {
                Card(
                    onClick = { navController.navigate(Screen.TaskDetail.createRoute(bestNextTask.id)) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(bestNextTask.title, fontWeight = FontWeight.Bold)
                        Text(
                            "Score ${String.format("%.0f", bestNextTask.score())}/100 - ${bestNextTask.deadlineLabel()}",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        TextButton(onClick = { navController.navigate(Screen.Focus.createRoute(bestNextTask.id)) }) {
                            Text("Start Focus")
                        }
                    }
                }
            }

            Text(
                "RISK ALERTS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ElevatedCard(onClick = { navController.navigate(Screen.FullRiskAlerts.route) }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("$highRiskCount high-risk task(s)", fontWeight = FontWeight.SemiBold)
                    Text("$overdueCount overdue task(s)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Open full risk alerts", color = MaterialTheme.colorScheme.primary)
                }
            }

            Text(
                "UPCOMING DEADLINES",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (upcomingDeadlines.isEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                    Text("No deadlines in the next 7 days.", modifier = Modifier.padding(14.dp))
                }
            } else {
                upcomingDeadlines.forEach { task ->
                    ElevatedCard(onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(task.title, modifier = Modifier.weight(1f))
                            Text(task.deadlineLabel(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            Text(
                "WEEKLY SUMMARY",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(
                onClick = { navController.navigate(Screen.FullWeeklySummary.route) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Completion ${(completionRate * 100).toInt()}% - Avg Progress $avgProgress%",
                        fontWeight = FontWeight.SemiBold
                    )
                    LinearProgressIndicator(
                        progress = { completionRate.coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                }
            }

            Text(
                "STUDY HOURS TRACKER",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "${String.format("%.1f", weeklyStudyHours)}h / ${String.format("%.0f", studyGoalHours)}h this week",
                        fontWeight = FontWeight.SemiBold
                    )
                    LinearProgressIndicator(
                        progress = { (weeklyStudyHours / studyGoalHours).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                }
            }
// ── Quick actions ─────────────────────────────────────────────────
            Text(
                "QUICK ACTIONS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon    = Icons.Default.Warning,
                    label   = "Risk Insights",
                    onClick = { navController.navigate(Screen.RiskInsights.route) },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor   = MaterialTheme.colorScheme.onErrorContainer
                )
                QuickActionCard(
                    icon    = Icons.Default.DateRange,
                    label   = "Weekly Review",
                    onClick = { navController.navigate(Screen.WeeklyReview.route) },
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.DateRange,
                    label = "Planner",
                    onClick = { navController.navigate(Screen.Planner.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Default.Warning,
                    label = "Alerts",
                    onClick = { navController.navigate(Screen.Notifications.route) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.BarChart,
                    label = "Analytics",
                    onClick = { navController.navigate(Screen.Stats.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Default.ViewAgenda,
                    label = "Dashboard Details",
                    onClick = { navController.navigate(Screen.DashboardDetails.route) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(80.dp)) // FAB clearance
        }
    }
}

@Composable
private fun StatsRow(taskCount: Int, atRisk: Int, completed: Int, sessionMin: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatTile(
            value    = taskCount.toString(),
            label    = "Total",
            modifier = Modifier.weight(1f)
        )
        StatTile(
            value    = completed.toString(),
            label    = "Done",
            color    = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        StatTile(
            value    = atRisk.toString(),
            label    = "At Risk",
            color    = if (atRisk > 0) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
        StatTile(
            value    = if (sessionMin > 0) "${sessionMin}m" else "—",
            label    = "Session",
            color    = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    ElevatedCard(
        modifier  = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text  = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color    = contentColor.copy(alpha = 0.12f),
                shape    = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = contentColor,
                        modifier           = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text  = label,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PanicBanner(onOpen: () -> Unit, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning, contentDescription = null,
                    tint     = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Survival Mode Active",
                    style    = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color    = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss",
                        tint     = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp))
                }
            }
            Text(
                "You have critical tasks due very soon. Open Survival Mode for your rescue plan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
            FilledTonalButton(
                onClick = onOpen,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Open Survival Mode", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun AdviceCard(
    localAdvice: String,
    ollamaState: AiState,
    onRefresh: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth().animateContentSize(),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color    = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                    shape    = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = when (ollamaState) {
                        is AiState.Success -> "AI Advice · OpenRouter"
                        is AiState.Loading -> "Thinking…"
                        else                   -> "Study Advice"
                    },
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier   = Modifier.weight(1f)
                )
                IconButton(
                    onClick  = onRefresh,
                    enabled  = ollamaState !is AiState.Loading,
                    modifier = Modifier.size(36.dp)
                ) {
                    if (ollamaState is AiState.Loading) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color       = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh, contentDescription = "Refresh",
                            tint     = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            when (ollamaState) {
                is AiState.Loading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ShimmerBox(Modifier.fillMaxWidth(0.9f), height = 16.dp)
                        ShimmerBox(Modifier.fillMaxWidth(0.7f), height = 16.dp)
                        ShimmerBox(Modifier.fillMaxWidth(0.8f), height = 16.dp)
                    }
                }
                is AiState.Success -> Text(
                    text  = ollamaState.response,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                is AiState.Failure -> {
                    Text(
                        text  = localAdvice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "OpenRouter error: ${ollamaState.message} · showing local advice",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
                is AiState.Unavailable -> {
                    Text(
                        text  = localAdvice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "OpenRouter not reachable · showing local advice",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                    )
                }
                else -> Text(
                    text  = localAdvice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
