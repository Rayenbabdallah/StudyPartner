package com.example.studypartner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyReviewScreen(navController: NavController, viewModel: StudyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val tasks   = (uiState as? TaskUiState.Success)?.tasks ?: emptyList()

    // Compute current week window (Mon–Sun)
    var weekOffset by remember { mutableIntStateOf(0) }
    val calendar   = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        add(Calendar.WEEK_OF_YEAR, weekOffset)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val weekStart  = calendar.timeInMillis
    val weekEnd    = weekStart + 7L * 24 * 60 * 60 * 1000

    val weekLabel = run {
        val fmt = SimpleDateFormat("MMM d", Locale.getDefault())
        val endFmt = SimpleDateFormat("d, yyyy", Locale.getDefault())
        "${fmt.format(Date(weekStart))} – ${endFmt.format(Date(weekEnd - 1))}"
    }

    // Group tasks by day
    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val dayTasks: List<Pair<String, List<StudyTask>>> = dayNames.mapIndexed { idx, day ->
        val dayStart = weekStart + idx * 24L * 60 * 60 * 1000
        val dayEnd   = dayStart + 24L * 60 * 60 * 1000
        val dayNum   = Calendar.getInstance().apply {
            timeInMillis = dayStart
        }.get(Calendar.DAY_OF_MONTH)
        "$day $dayNum" to tasks.filter { task ->
            task.deadline != null &&
            task.deadline >= dayStart &&
            task.deadline < dayEnd
        }
    }

    val completionRate = if (tasks.isNotEmpty())
        tasks.count { it.isCompleted }.toFloat() / tasks.size else 0f
    val avgProgress = if (tasks.isNotEmpty())
        tasks.map { it.progress }.average().toInt() else 0

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
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->

        LazyColumn(
            contentPadding = PaddingValues(
                start  = 24.dp, end = 24.dp,
                top    = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Editorial header ──────────────────────────────────────────────
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "ACADEMIC FLOW",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Weekly\nRhythm.",
                        style     = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color     = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Optimize your cognitive load across the week. Balance your focus sessions for peak performance.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(20.dp))
            }

            // ── Week navigation ───────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { weekOffset-- }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous week",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Text(
                            weekLabel,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { weekOffset++ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next week",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Day rows ──────────────────────────────────────────────────────
            items(dayTasks) { (dayLabel, dayTaskList) ->
                val isToday = run {
                    val now = Calendar.getInstance()
                    val parts = dayLabel.split(" ")
                    val dayNum = parts.lastOrNull()?.toIntOrNull() ?: -1
                    now.get(Calendar.DAY_OF_MONTH) == dayNum && weekOffset == 0
                }
                val hasHighRisk = dayTaskList.any { it.isHighRisk() && !it.isCompleted }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Day label column
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(48.dp)
                    ) {
                        val parts = dayLabel.split(" ")
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isToday) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceContainerLow
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                parts.lastOrNull() ?: "",
                                style     = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color     = if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            parts.firstOrNull() ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Tasks for the day
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (dayTaskList.isEmpty()) {
                            Text(
                                if (isToday) "Free day — enjoy the break." else "—",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            dayTaskList.forEach { task ->
                                WeekDayTaskChip(task = task)
                            }
                        }
                        // risk indicator dot
                        if (hasHighRisk) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error)
                                )
                                Text(
                                    "High priority",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // ── Cognitive Health section ──────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    "COGNITIVE HEALTH",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    shape  = RoundedCornerShape(20.dp),
                    color  = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Overall Progress",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "$avgProgress%",
                                style     = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color     = MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress   = { avgProgress / 100f },
                            modifier   = Modifier.fillMaxWidth().height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color      = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "COMPLETION",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${(completionRate * 100).toInt()}%",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = riskColorSet("Safe").text
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "TASKS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "${tasks.size} total",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        if (tasks.isNotEmpty()) {
                            val insight = when {
                                completionRate >= 0.8f -> "Outstanding! You're ahead of schedule. Consider tackling bonus material."
                                completionRate >= 0.5f -> "Good momentum. Keep consistent daily sessions to maintain your pace."
                                else -> "Focus on one task at a time. Break large tasks into 25-min sessions."
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp))
                                    Text(
                                        insight,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekDayTaskChip(task: StudyTask) {
    val riskColors = riskColorSet(task.riskLabel())
    val isDone     = task.isCompleted

    Surface(
        shape    = RoundedCornerShape(10.dp),
        color    = if (isDone) MaterialTheme.colorScheme.surfaceContainerLow
                   else riskColors.background.copy(alpha = 0.7f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isDone) MaterialTheme.colorScheme.secondary
                       else riskColors.text,
                modifier = Modifier.size(14.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    task.subject,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isDone) {
                Text(
                    "${task.progress}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = riskColors.text
                )
            }
        }
    }
}
