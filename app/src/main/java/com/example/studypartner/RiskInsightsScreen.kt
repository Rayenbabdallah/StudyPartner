package com.example.studypartner

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.studypartner.ui.theme.BookmarkGold
import com.example.studypartner.ui.theme.DeepOrange
import com.example.studypartner.ui.theme.Dimens
import com.example.studypartner.ui.theme.LimeCheck
import com.example.studypartner.ui.theme.OrangeCheck
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskInsightsScreen(navController: NavController, viewModel: StudyViewModel) {

    val uiState by viewModel.uiState.collectAsState()
    val courses by viewModel.allCourses.collectAsState()
    val tasks   = (uiState as? TaskUiState.Success)?.tasks ?: emptyList()
    val active  = remember(tasks) { tasks.activeTasks() }

    val overdue       = remember(active) { active.overdueTasks() }
    val critical      = remember(active) { active.criticalTasks() }
    val courseRisks   = remember(courses, tasks) {
        courses.map { course ->
            val score = viewModel.courseRiskScore(course)
            Triple(course, score, tasks.filter { it.courseId == course.id })
        }.sortedByDescending { it.second }
    }
    val uncategorised = remember(active) { active.filter { it.courseId == null } }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Risk Insights", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->

        LazyColumn(
            contentPadding = PaddingValues(
                start  = Dimens.ScreenPadding,
                end    = Dimens.ScreenPadding,
                top    = padding.calculateTopPadding() + Dimens.SpaceXs,
                bottom = padding.calculateBottomPadding() + Dimens.SpaceLg
            ),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
        ) {

            // Hero
            item {
                Column(modifier = Modifier.padding(top = Dimens.SpaceSm)) {
                    Text(
                        "AT RISK",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Dimens.SpaceXs))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text       = "${critical.size}",
                            fontSize   = 72.sp,
                            lineHeight = 76.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = if (critical.isNotEmpty()) MaterialTheme.colorScheme.error
                                         else LimeCheck
                        )
                        Spacer(Modifier.width(Dimens.SpaceMd))
                        Column(modifier = Modifier.padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "critical",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${overdue.size} overdue · ${active.size} active",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (overdue.isNotEmpty() || critical.isNotEmpty()) {
                item { AlertBanner(overdue = overdue.size, critical = critical.size) }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                ) {
                    RiskNavPill("Course Risk", DeepOrange) { navController.navigate(Screen.CourseRiskBreakdown.route) }
                    RiskNavPill("Deadlines",   OrangeCheck) { navController.navigate(Screen.DeadlineRiskDetails.route) }
                    RiskNavPill("Overload",    BookmarkGold) { navController.navigate(Screen.OverloadAnalysis.route) }
                    RiskNavPill("Readiness",   LimeCheck) { navController.navigate(Screen.StudyReadiness.route) }
                }
            }

            item { RiskBreakdownCard(tasks = active) }

            if (courseRisks.isNotEmpty()) {
                item {
                    Text(
                        "Course Heatmap",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary,
                        modifier   = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(courseRisks) { (course, score, courseTasks) ->
                    CourseRiskCard(course = course, riskScore = score, tasks = courseTasks)
                }
            }

            if (uncategorised.isNotEmpty()) {
                item {
                    Text(
                        "No Course Assigned (${uncategorised.size})",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary,
                        modifier   = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(uncategorised) { task ->
                    UncategorisedTaskRow(task)
                }
            }

            if (courseRisks.isEmpty() && uncategorised.isEmpty()) {
                item {
                    EmptyStateView(
                        icon     = Icons.Default.Shield,
                        title    = "All clear!",
                        subtitle = "No active tasks to analyse. Add some tasks to see risk insights.",
                        modifier = Modifier.padding(top = 40.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskNavPill(label: String, accent: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun AlertBanner(overdue: Int, critical: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer),
        shape    = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Warning, contentDescription = null,
                tint     = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                if (overdue > 0) Text(
                    "$overdue overdue task(s) need immediate attention",
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onErrorContainer
                )
                if (critical > 0) Text(
                    "$critical task(s) scored Critical (≥ 80/100)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun RiskBreakdownCard(tasks: List<StudyTask>) {
    val critical  = tasks.count { it.score() >= 80 }
    val high      = tasks.count { it.score() in 60.0..<80.0 }
    val medium    = tasks.count { it.score() in 40.0..<60.0 }
    val low       = tasks.count { it.score() < 40 }
    val highRisk  = riskColorSet("High Risk")
    val medRisk   = riskColorSet("Moderate")
    val safeRisk  = riskColorSet("Safe")

    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Priority Distribution",
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
            PriorityRow("Critical", critical, tasks.size, highRisk.text)
            PriorityRow("High",     high,     tasks.size, medRisk.text)
            PriorityRow("Medium",   medium,   tasks.size, MaterialTheme.colorScheme.primary)
            PriorityRow("Low",      low,      tasks.size, safeRisk.text)
        }
    }
}

@Composable
private fun PriorityRow(label: String, count: Int, total: Int, color: Color) {
    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animate = true }
    val progress by animateFloatAsState(
        targetValue   = if (animate && total > 0) count.toFloat() / total else 0f,
        animationSpec = tween(700),
        label         = "priority_$label"
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style    = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(60.dp),
            color    = MaterialTheme.colorScheme.onSurface
        )
        LinearProgressIndicator(
            progress   = { progress },
            modifier   = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
            color      = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.width(8.dp))
        Text(
            count.toString(),
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = color,
            modifier   = Modifier.width(24.dp)
        )
    }
}

@Composable
private fun CourseRiskCard(course: Course, riskScore: Double, tasks: List<StudyTask>) {
    val courseColor = runCatching {
        Color(android.graphics.Color.parseColor(course.colorHex))
    }.getOrDefault(MaterialTheme.colorScheme.primary)

    val riskLabel = when {
        riskScore >= 60 -> "High Risk"
        riskScore >= 40 -> "Moderate"
        else            -> "Safe"
    }
    val colors = riskColorSet(riskLabel)

    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animate = true }
    val bar by animateFloatAsState(
        targetValue   = if (animate) (riskScore / 100f).toFloat() else 0f,
        animationSpec = tween(800),
        label         = "course_risk_${course.id}"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = colors.background),
        shape    = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(courseColor)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    course.title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    modifier   = Modifier.weight(1f)
                )
                Surface(
                    color = colors.text.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        riskLabel,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = colors.text,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress   = { bar },
                modifier   = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color      = colors.text,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Risk: ${String.format("%.0f", riskScore)}/100",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${tasks.count { !it.isCompleted }} active",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UncategorisedTaskRow(task: StudyTask) {
    val colors = scoreToRiskColorSet(task.score())
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(task.taskType.label, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${String.format("%.0f", task.score())}/100",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color      = colors.text
                )
                Text(task.deadlineLabel(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
