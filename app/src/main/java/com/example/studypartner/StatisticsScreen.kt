package com.example.studypartner

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    outerPadding: PaddingValues = PaddingValues()
) {
    val context    = LocalContext.current
    val state      by viewModel.uiState.collectAsState()
    val tasks      = (state as? TaskUiState.Success)?.tasks ?: emptyList()
    val panicCount by UserPreferences.panicModeCount(context).collectAsState(initial = 0)

    val total        = tasks.size
    val completed    = tasks.count { it.isCompleted }
    val active       = total - completed
    val atRisk       = tasks.count { it.isHighRisk() && !it.isCompleted }
    val rate         = if (total > 0) completed.toFloat() / total else 0f
    val avgProgress  = if (tasks.isNotEmpty()) tasks.map { it.progress }.average().toInt() else 0

    val breakdown    = viewModel.subjectBreakdown()
    val maxScore     = breakdown.values.maxOrNull() ?: 1.0
    val sessionMin   = viewModel.recommendedSession()

    var animate by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animate = true }

    val animatedRate by animateFloatAsState(
        targetValue   = if (animate) rate else 0f,
        animationSpec = tween(900),
        label         = "completionRate"
    )

    // Theme-aware semantic colors (via helper)
    val safeColors = riskColorSet("Safe")
    val medColors  = riskColorSet("Moderate")
    val highColors = riskColorSet("High Risk")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Editorial header ──────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "PERFORMANCE ANALYTICS",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Study Time\nDistribution.",
                    style     = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color     = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(
                    onClick = { navController.navigate(Screen.StudyTimeAnalytics.route) },
                    label = { Text("Study Time") }
                )
                AssistChip(
                    onClick = { navController.navigate(Screen.TaskCompletionStats.route) },
                    label = { Text("Task Stats") }
                )
                AssistChip(
                    onClick = { navController.navigate(Screen.CoursePerformance.route) },
                    label = { Text("Course Perf") }
                )
                AssistChip(
                    onClick = { navController.navigate(Screen.WeeklyReview.route) },
                    label = { Text("Weekly Review") }
                )
            }

            // ── Overview tiles ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniStatCard("Total",    total.toString(),  Modifier.weight(1f))
                MiniStatCard("Active",   active.toString(), Modifier.weight(1f),
                    valueColor = MaterialTheme.colorScheme.primary)
                MiniStatCard("Done",     completed.toString(), Modifier.weight(1f),
                    valueColor = safeColors.text)
                MiniStatCard("Progress", "$avgProgress%",  Modifier.weight(1f),
                    valueColor = if (avgProgress >= 70) safeColors.text
                                 else MaterialTheme.colorScheme.primary)
            }

            // ── Panic count badge ─────────────────────────────────────────────
            if (panicCount > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors   = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    shape    = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.EmojiEvents, contentDescription = null,
                            tint     = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text("Survival Mode Activations",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer)
                            Text("$panicCount time(s) this session",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }
            }

            // ── Completion rate ───────────────────────────────────────────────
            StatSection(title = "Completion Rate") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "$completed of $total tasks done",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${(rate * 100).toInt()}%",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = safeColors.text
                        )
                    }
                    LinearProgressIndicator(
                        progress   = { animatedRate },
                        modifier   = Modifier.fillMaxWidth().height(12.dp),
                        color      = safeColors.text,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            // ── Subject breakdown ─────────────────────────────────────────────
            if (breakdown.isNotEmpty()) {
                StatSection(title = "Subject Breakdown") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        breakdown.entries.forEachIndexed { index, (subject, score) ->
                            val subjectProgress by animateFloatAsState(
                                targetValue   = if (animate) (score / maxScore).toFloat() else 0f,
                                animationSpec = tween(900 + index * 100),
                                label         = "subject_$subject"
                            )
                            SubjectBar(
                                subject  = subject,
                                score    = score,
                                progress = subjectProgress,
                                color    = subjectBarColor(index)
                            )
                        }
                    }
                }
            }

            // ── Risk distribution ─────────────────────────────────────────────
            StatSection(title = "Risk Distribution") {
                val highRisk = tasks.count { it.riskLabel() == "High Risk" && !it.isCompleted }
                val moderate = tasks.count { it.riskLabel() == "Moderate"  && !it.isCompleted }
                val safe     = tasks.count { it.riskLabel() == "Safe"      && !it.isCompleted }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RiskDistributionRow("High Risk", highRisk, highColors.text)
                    RiskDistributionRow("Moderate",  moderate, medColors.text)
                    RiskDistributionRow("Safe",      safe,     safeColors.text)
                }
            }

            // ── Recommended session ───────────────────────────────────────────
            StatSection(title = "Recommended Study Session") {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Based on your current workload",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        if (sessionMin > 0) "$sessionMin min" else "No tasks",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text  = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        Surface(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            color     = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 0.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun MiniStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    ElevatedCard(
        modifier  = modifier,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = value,
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = valueColor
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
private fun SubjectBar(subject: String, score: Double, progress: Float, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(subject, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface)
            Text(
                String.format("%.1f pts", score),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress   = { progress },
            modifier   = Modifier.fillMaxWidth().height(8.dp),
            color      = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun RiskDistributionRow(label: String, count: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color    = color.copy(alpha = 0.15f),
                shape    = RoundedCornerShape(6.dp),
                modifier = Modifier.size(width = 12.dp, height = 24.dp)
            ) {}
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            count.toString(),
            style      = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color      = color
        )
    }
}

@Composable
private fun subjectBarColor(index: Int): Color = when (index % 5) {
    0    -> MaterialTheme.colorScheme.primary
    1    -> MaterialTheme.colorScheme.secondary
    2    -> MaterialTheme.colorScheme.tertiary
    3    -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.outline
}

