package com.example.studypartner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studypartner.ui.theme.BookmarkGold
import com.example.studypartner.ui.theme.DeepOrange
import com.example.studypartner.ui.theme.Dimens
import com.example.studypartner.ui.theme.LimeCheck
import com.example.studypartner.ui.theme.OrangeCheck
import com.example.studypartner.ui.theme.SoftGold
import com.example.studypartner.ui.theme.WarmAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var panicBannerDismissed by rememberSaveable { mutableStateOf(false) }

    val localAdvice = when (state) {
        is TaskUiState.Loading -> "Analysing your tasks…"
        is TaskUiState.Success -> viewModel.getAdvice()
        is TaskUiState.Error   -> (state as TaskUiState.Error).message
    }
    val tasks = (state as? TaskUiState.Success)?.tasks ?: emptyList()

    val bestNextTask = remember(tasks) {
        viewModel.sortedTasks().firstOrNull { !it.isCompleted }
    }
    val todayTasks = remember(tasks) {
        tasks.filter { !it.isCompleted && it.daysUntilDeadline() == 0 }
    }
    val upcomingDeadlines = remember(tasks) {
        tasks.asSequence()
            .filter { !it.isCompleted }
            .filter { task ->
                val days = task.daysUntilDeadline()
                days != null && days in 1..14
            }
            .sortedBy { it.deadline ?: Long.MAX_VALUE }
            .toList()
    }
    val highRiskCount = remember(tasks) { tasks.count { !it.isCompleted && it.isHighRisk() } }
    val overdueCount  = remember(tasks) { tasks.count { !it.isCompleted && it.isOverdue() } }
    val activeCount   = remember(tasks) { tasks.count { !it.isCompleted } }
    val completionRate = remember(tasks) {
        if (tasks.isNotEmpty()) tasks.count { it.isCompleted }.toFloat() / tasks.size else 0f
    }
    val weekStartMillis = remember {
        java.util.Calendar.getInstance().apply {
            firstDayOfWeek = java.util.Calendar.MONDAY
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val weeklyStudyMinutes = remember(sessions, weekStartMillis) {
        sessions.filter { it.updatedAt >= weekStartMillis }.sumOf { it.durationMinutes }
    }
    val weeklyStudyHours = weeklyStudyMinutes / 60f
    val studyGoalHours   = 12f
    val hoursProgress    = (weeklyStudyHours / studyGoalHours).coerceIn(0f, 1f)

    val today = remember {
        SimpleDateFormat("EEEE · MMM d", Locale.getDefault())
            .format(Date()).uppercase()
    }

    LaunchedEffect(tasks.size) {
        if (tasks.isNotEmpty() && ollamaState is AiState.Idle) {
            viewModel.fetchOllamaAdvice()
        }
    }

    // Staggered entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DeepOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "StudyPartner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = "Notifications")
                    }
                    IconButton(onClick = { navController.navigate(Screen.AiAssistant.route) }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Assistant")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.Add.route) },
                icon    = { Icon(Icons.Default.Add, contentDescription = null) },
                text    = { Text("New task", style = MaterialTheme.typography.labelLarge) },
                containerColor = DeepOrange,
                contentColor   = Color.White
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
                .padding(horizontal = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg)
        ) {
            Spacer(Modifier.height(Dimens.SpaceXs))

            // ── Panic banner ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = appMode == AppMode.GHASRET_LEKLEB && !panicBannerDismissed,
                enter   = expandVertically() + fadeIn(),
                exit    = shrinkVertically() + fadeOut()
            ) {
                PanicRibbon(
                    onOpen    = { navController.navigate(Screen.GhasretActivation.route) },
                    onDismiss = { panicBannerDismissed = true; viewModel.dismissPanic() }
                )
            }

            // ── HERO ──────────────────────────────────────────────────────────
            FadeIn(visible = visible, delayMs = 0) {
                HeroBlock(
                    dateLabel    = today,
                    todayCount   = todayTasks.size,
                    overdueCount = overdueCount,
                    activeCount  = activeCount,
                    onTodayClick = { navController.navigate(Screen.TodayTasks.route) }
                )
            }

            // ── BENTO: Best Next | (Hours ring + AI Advice) ───────────────────
            FadeIn(visible = visible, delayMs = 100) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
                ) {
                    BestNextCard(
                        task          = bestNextTask,
                        onOpenDetail  = { id -> navController.navigate(Screen.TaskDetail.createRoute(id)) },
                        onStartFocus  = { id -> navController.navigate(Screen.Focus.createRoute(id)) },
                        modifier      = Modifier.weight(1.15f).fillMaxHeight()
                    )
                    Column(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
                    ) {
                        StudyRingCard(
                            hours      = weeklyStudyHours,
                            goal       = studyGoalHours,
                            progress   = hoursProgress,
                            onClick    = { navController.navigate(Screen.FullWeeklySummary.route) },
                            modifier   = Modifier.fillMaxWidth().weight(1f)
                        )
                        CompletionPill(
                            rate     = completionRate,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── DEADLINE TIMELINE ─────────────────────────────────────────────
            FadeIn(visible = visible, delayMs = 180) {
                SectionEyebrow("UPCOMING", trailing = "View all") {
                    navController.navigate(Screen.UpcomingTasks.route)
                }
            }
            FadeIn(visible = visible, delayMs = 220) {
                if (upcomingDeadlines.isEmpty() && todayTasks.isEmpty()) {
                    PaperEmptyCard("No deadlines in the next two weeks. Use the calm.")
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
                    ) {
                        if (todayTasks.isNotEmpty()) {
                            DeadlineChip(
                                day       = "TODAY",
                                count     = todayTasks.size,
                                title     = todayTasks.first().title,
                                accent    = DeepOrange,
                                isUrgent  = true,
                                onClick   = { navController.navigate(Screen.TaskDetail.createRoute(todayTasks.first().id)) }
                            )
                        }
                        upcomingDeadlines.take(8).forEach { task ->
                            val days = task.daysUntilDeadline() ?: 0
                            val accent = when {
                                days <= 1 -> DeepOrange
                                days <= 3 -> OrangeCheck
                                days <= 7 -> WarmAmber
                                else      -> BookmarkGold
                            }
                            DeadlineChip(
                                day      = dayLabel(task.deadline),
                                count    = 1,
                                title    = task.title,
                                accent   = accent,
                                isUrgent = days <= 1,
                                onClick  = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }
                            )
                        }
                    }
                }
            }

            // ── RISK STRIP ────────────────────────────────────────────────────
            FadeIn(visible = visible, delayMs = 280) {
                RiskStrip(
                    high    = highRiskCount,
                    overdue = overdueCount,
                    onClick = { navController.navigate(Screen.RiskInsights.route) }
                )
            }

            // ── AI ADVICE ─────────────────────────────────────────────────────
            FadeIn(visible = visible, delayMs = 340) {
                AiAdviceCard(
                    localAdvice = localAdvice,
                    ollamaState = ollamaState,
                    onRefresh   = { viewModel.fetchOllamaAdvice() },
                    onOpen      = { navController.navigate(Screen.AiAssistant.route) }
                )
            }

            // ── QUICK ACTIONS BENTO ───────────────────────────────────────────
            FadeIn(visible = visible, delayMs = 400) {
                SectionEyebrow("EXPLORE")
            }
            FadeIn(visible = visible, delayMs = 440) {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                        BookmarkActionCard(
                            icon       = Icons.Default.Insights,
                            label      = "Risk\nInsights",
                            ribbon     = DeepOrange,
                            background = MaterialTheme.colorScheme.errorContainer,
                            modifier   = Modifier.weight(1f),
                            onClick    = { navController.navigate(Screen.RiskInsights.route) }
                        )
                        BookmarkActionCard(
                            icon       = Icons.Default.DateRange,
                            label      = "Weekly\nReview",
                            ribbon     = LimeCheck,
                            background = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier   = Modifier.weight(1f),
                            onClick    = { navController.navigate(Screen.WeeklyReview.route) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                        BookmarkActionCard(
                            icon       = Icons.Default.BarChart,
                            label      = "Analytics",
                            ribbon     = BookmarkGold,
                            background = MaterialTheme.colorScheme.primaryContainer,
                            modifier   = Modifier.weight(1f),
                            onClick    = { navController.navigate(Screen.Stats.route) }
                        )
                        BookmarkActionCard(
                            icon       = Icons.Default.Timer,
                            label      = "Planner",
                            ribbon     = OrangeCheck,
                            background = MaterialTheme.colorScheme.secondaryContainer,
                            modifier   = Modifier.weight(1f),
                            onClick    = { navController.navigate(Screen.Planner.route) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                        BookmarkActionCard(
                            icon       = Icons.Default.NotificationsActive,
                            label      = "Alerts",
                            ribbon     = WarmAmber,
                            background = MaterialTheme.colorScheme.surfaceContainer,
                            modifier   = Modifier.weight(1f),
                            onClick    = { navController.navigate(Screen.Notifications.route) }
                        )
                        BookmarkActionCard(
                            icon       = Icons.Default.ViewAgenda,
                            label      = "Dashboard",
                            ribbon     = SoftGold,
                            background = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier   = Modifier.weight(1f),
                            onClick    = { navController.navigate(Screen.DashboardDetails.route) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(96.dp))
        }
    }
}

// ─── HERO BLOCK ──────────────────────────────────────────────────────────────

@Composable
private fun HeroBlock(
    dateLabel: String,
    todayCount: Int,
    overdueCount: Int,
    activeCount: Int,
    onTodayClick: () -> Unit
) {
    val animatedToday by animateIntAsState(
        targetValue = todayCount,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "todayCount"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTodayClick() }
            .padding(vertical = Dimens.SpaceSm)
    ) {
        Text(
            text  = dateLabel,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Dimens.SpaceXs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text       = animatedToday.toString(),
                fontSize   = 88.sp,
                lineHeight = 92.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onSurface,
                modifier   = Modifier.alignByBaseline()
            )
            Spacer(Modifier.width(Dimens.SpaceMd))
            Column(
                modifier = Modifier.alignByBaseline().padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text  = if (todayCount == 1) "task due" else "tasks due",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = "today",
                    style = MaterialTheme.typography.titleMedium,
                    color = DeepOrange,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        Spacer(Modifier.height(Dimens.SpaceMd))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            HeroPill(
                icon  = Icons.Default.Bolt,
                value = "$activeCount",
                label = "active",
                tint  = DeepOrange
            )
            if (overdueCount > 0) {
                HeroPill(
                    icon  = Icons.Default.LocalFireDepartment,
                    value = "$overdueCount",
                    label = "overdue",
                    tint  = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun HeroPill(icon: ImageVector, value: String, label: String, tint: Color) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ─── BENTO: Best Next ────────────────────────────────────────────────────────

@Composable
private fun BestNextCard(
    task: StudyTask?,
    onOpenDetail: (Int) -> Unit,
    onStartFocus: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color    = MaterialTheme.colorScheme.primaryContainer,
        shape    = RoundedCornerShape(24.dp),
        tonalElevation = 0.dp
    ) {
        if (task == null) {
            Column(
                modifier = Modifier.padding(Dimens.CardPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
            ) {
                Text(
                    "ALL CLEAR",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    "No active tasks",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Column(
                modifier = Modifier.padding(Dimens.CardPadding).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(DeepOrange)
                    )
                    Text(
                        "NEXT UP",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 3
                )
                Spacer(Modifier.weight(1f, fill = false))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RingProgress(
                        progress = (task.score().toFloat() / 100f).coerceIn(0f, 1f),
                        size = 56.dp,
                        strokeWidth = 6.dp,
                        color = DeepOrange,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${task.score().toInt()}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.deadlineLabel(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "score / 100",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        )
                    }
                }
                Spacer(Modifier.height(Dimens.SpaceXs))
                Button(
                    onClick = { onStartFocus(task.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepOrange,
                        contentColor   = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Start focus", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                TextButton(
                    onClick = { onOpenDetail(task.id) },
                    modifier = Modifier.align(Alignment.End),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        "Details →",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ─── BENTO: Study hours ring ─────────────────────────────────────────────────

@Composable
private fun StudyRingCard(
    hours: Float,
    goal: Float,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable { onClick() },
        color    = MaterialTheme.colorScheme.surfaceContainer,
        shape    = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceMd).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "THIS WEEK",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                RingProgress(
                    progress    = progress,
                    size        = 84.dp,
                    strokeWidth = 9.dp,
                    color       = LimeCheck,
                    trackColor  = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = String.format(Locale.getDefault(), "%.1f", hours),
                            fontSize   = 22.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text  = "/ ${goal.toInt()}h",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionPill(rate: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(
        targetValue   = rate,
        animationSpec = tween(900),
        label         = "rate"
    )
    Surface(
        modifier = modifier,
        color    = MaterialTheme.colorScheme.tertiaryContainer,
        shape    = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(Dimens.SpaceMd),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "${(animated * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    "done",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
            LinearProgressIndicator(
                progress   = { animated },
                modifier   = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color      = LimeCheck,
                trackColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.12f)
            )
        }
    }
}

// ─── DEADLINE CHIP ───────────────────────────────────────────────────────────

@Composable
private fun DeadlineChip(
    day: String,
    count: Int,
    title: String,
    accent: Color,
    isUrgent: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        color    = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape    = RoundedCornerShape(18.dp),
        border   = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Text(
                    text  = day,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUrgent) accent else MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text  = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            if (count > 1) {
                Text(
                    text  = "+ ${count - 1} more",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─── RISK STRIP ──────────────────────────────────────────────────────────────

@Composable
private fun RiskStrip(high: Int, overdue: Int, onClick: () -> Unit) {
    val empty = high == 0 && overdue == 0
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        color    = if (empty) MaterialTheme.colorScheme.tertiaryContainer
                   else MaterialTheme.colorScheme.errorContainer,
        shape    = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.CardPadding, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (empty) Icons.Default.AutoAwesome else Icons.Default.Warning,
                contentDescription = null,
                tint = if (empty) MaterialTheme.colorScheme.onTertiaryContainer
                       else MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(Dimens.SpaceSm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (empty) "No risk alerts"
                           else "$high high-risk · $overdue overdue",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (empty) MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = if (empty) "Keep the streak going."
                           else "Tap to triage in Risk Insights.",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (empty) MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
            Text(
                "→",
                style = MaterialTheme.typography.titleMedium,
                color = if (empty) MaterialTheme.colorScheme.onTertiaryContainer
                        else MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

// ─── AI ADVICE CARD ──────────────────────────────────────────────────────────

@Composable
private fun AiAdviceCard(
    localAdvice: String,
    ollamaState: AiState,
    onRefresh: () -> Unit,
    onOpen: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        color    = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape    = RoundedCornerShape(20.dp),
        border   = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(Dimens.CardPadding)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(BookmarkGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF3D2400),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = when (ollamaState) {
                        is AiState.Success -> "AI · OpenRouter"
                        is AiState.Loading -> "AI thinking…"
                        else                   -> "Today's advice"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onRefresh,
                    enabled = ollamaState !is AiState.Loading,
                    modifier = Modifier.size(32.dp)
                ) {
                    if (ollamaState is AiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = DeepOrange
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(Dimens.SpaceMd))
            when (ollamaState) {
                is AiState.Loading -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerBox(Modifier.fillMaxWidth(0.9f), height = 14.dp)
                    ShimmerBox(Modifier.fillMaxWidth(0.7f), height = 14.dp)
                    ShimmerBox(Modifier.fillMaxWidth(0.85f), height = 14.dp)
                }
                is AiState.Success -> Text(
                    text = ollamaState.response,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                else -> Text(
                    text = localAdvice,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(Dimens.SpaceSm))
            TextButton(
                onClick = onOpen,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text(
                    "Ask the assistant →",
                    style = MaterialTheme.typography.labelMedium,
                    color = DeepOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ─── BOOKMARK ACTION CARD ────────────────────────────────────────────────────

@Composable
private fun BookmarkActionCard(
    icon: ImageVector,
    label: String,
    ribbon: Color,
    background: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() },
        color    = background,
        shape    = RoundedCornerShape(18.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Bookmark ribbon — top-right
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 0.dp, end = 14.dp)
                    .width(10.dp)
                    .height(22.dp)
                    .background(ribbon, shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

// ─── PANIC RIBBON ────────────────────────────────────────────────────────────

@Composable
private fun PanicRibbon(onOpen: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.errorContainer,
        shape    = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.width(Dimens.SpaceSm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Survival mode active",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Tap for your rescue plan.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
            FilledTonalButton(
                onClick = onOpen,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor   = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Open", style = MaterialTheme.typography.labelMedium) }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

// ─── HELPERS ─────────────────────────────────────────────────────────────────

@Composable
private fun SectionEyebrow(label: String, trailing: String? = null, onTrailingClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        if (trailing != null && onTrailingClick != null) {
            TextButton(
                onClick = onTrailingClick,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text(
                    "$trailing →",
                    style = MaterialTheme.typography.labelMedium,
                    color = DeepOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PaperEmptyCard(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.surfaceContainerLow,
        shape    = RoundedCornerShape(16.dp),
        border   = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Text(
            text  = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(Dimens.CardPadding)
        )
    }
}

@Composable
fun RingProgress(
    progress: Float,
    size: Dp,
    strokeWidth: Dp,
    color: Color,
    trackColor: Color,
    centerContent: @Composable BoxScope.() -> Unit = {}
) {
    val animated by animateFloatAsState(
        targetValue   = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label         = "ring"
    )
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val sw = strokeWidth.toPx()
            val arcSize = Size(this.size.width - sw, this.size.height - sw)
            val topLeft = Offset(sw / 2, sw / 2)
            drawArc(
                color      = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter  = false,
                style      = Stroke(width = sw, cap = StrokeCap.Round),
                size       = arcSize,
                topLeft    = topLeft
            )
            drawArc(
                color      = color,
                startAngle = -90f,
                sweepAngle = 360f * animated,
                useCenter  = false,
                style      = Stroke(width = sw, cap = StrokeCap.Round),
                size       = arcSize,
                topLeft    = topLeft
            )
        }
        centerContent()
    }
}

@Composable
private fun FadeIn(visible: Boolean, delayMs: Int, content: @Composable () -> Unit) {
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "fadeIn-$delayMs"
    )
    val offsetY by animateFloatAsState(
        targetValue   = if (visible) 0f else 16f,
        animationSpec = tween(durationMillis = 500, delayMillis = delayMs, easing = FastOutSlowInEasing),
        label         = "slideUp-$delayMs"
    )
    Box(
        modifier = Modifier
            .alpha(alpha)
            .offset(y = offsetY.dp)
    ) { content() }
}

private fun dayLabel(deadline: Long?): String {
    if (deadline == null) return "—"
    val now = System.currentTimeMillis()
    val deltaDays = ((deadline - now) / (1000L * 60 * 60 * 24)).toInt()
    return when {
        deltaDays <= 0 -> "TODAY"
        deltaDays == 1 -> "TOMORROW"
        deltaDays < 7  -> SimpleDateFormat("EEE", Locale.getDefault())
                            .format(Date(deadline)).uppercase()
        else            -> SimpleDateFormat("MMM d", Locale.getDefault())
                            .format(Date(deadline)).uppercase()
    }
}
