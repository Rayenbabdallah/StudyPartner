package com.example.studypartner

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailScreen(navController: NavController, viewModel: StudyViewModel, courseId: Int) {

    val courses  by viewModel.allCourses.collectAsState()
    val state    by viewModel.uiState.collectAsState()
    val allTasks = (state as? TaskUiState.Success)?.tasks ?: emptyList()

    val course = courses.find { it.id == courseId } ?: return
    val courseTasks = allTasks.filter { it.courseId == courseId }
    val active  = courseTasks.filter { !it.isCompleted }
    val done    = courseTasks.filter { it.isCompleted }

    val readinessScore = viewModel.studyReadinessScore(course)
    val riskScore      = viewModel.courseRiskScore(course)
    val readinessLabel = PriorityEngine.readinessLabel(readinessScore)
    val suggestedSessionPlan = viewModel.suggestStudySessionPlan(task = null, course = course)

    val readinessColors = when (readinessLabel) {
        "Ready"   -> riskColorSet("Safe")
        "At Risk" -> riskColorSet("Moderate")
        else      -> riskColorSet("High Risk")
    }

    val courseColor = runCatching {
        Color(android.graphics.Color.parseColor(course.colorHex))
    }.getOrDefault(MaterialTheme.colorScheme.primary)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.EditCourse.createRoute(courseId))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit course")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Editorial hero: course title + risk bento side ────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // Course code eyebrow
                        if (course.instructor.isNotBlank()) {
                            Text(
                                course.instructor.uppercase(),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                        // Large display title
                        Text(
                            course.title,
                            style     = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color     = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Credit weight: ${String.format("%.0f", course.creditWeight * 100)}%  ·  ${active.size} active task(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Risk badge card (asymmetric side column)
                    Surface(
                        shape    = RoundedCornerShape(20.dp),
                        color    = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(
                            modifier = Modifier
                                .width(110.dp)
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "READINESS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "$readinessScore%",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = readinessColors.text
                            )
                            Text(
                                readinessLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = readinessColors.text,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // ── Action chips ──────────────────────────────────────────────────
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        onClick  = {
                            val plan = viewModel.startSessionForCourse(courseId)
                            viewModel.saveStudySession(plan = plan, name = plan.contextLabel)
                            navController.navigate(Screen.QuickFocusMode.route)
                        },
                        shape    = RoundedCornerShape(12.dp),
                        color    = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp))
                            Text(
                                "Start Studying",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Surface(
                        shape    = RoundedCornerShape(12.dp),
                        color    = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.BarChart, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(16.dp))
                            Text(
                                "Risk: ${String.format("%.0f", riskScore)}/100",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("AI Session Suggestion", fontWeight = FontWeight.SemiBold)
                        Text(
                            "${suggestedSessionPlan.durationMinutes} min focus, " +
                                "${suggestedSessionPlan.breakLengthMinutes} min break every ${suggestedSessionPlan.breakEveryMinutes} min",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                TextButton(
                    onClick = {
                        navController.navigate(
                            Screen.AddStudySession.createRoute(courseId = courseId)
                        )
                    }
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Customize Session + Breaks")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { navController.navigate(Screen.CourseAssignments.createRoute(courseId)) }) {
                        Text("Assignments")
                    }
                    TextButton(onClick = { navController.navigate(Screen.CourseProgress.createRoute(courseId)) }) {
                        Text("Progress")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { navController.navigate(Screen.CourseExamInfo.createRoute(courseId)) }) {
                        Text("Exam Info")
                    }
                    TextButton(onClick = { navController.navigate(Screen.CourseStudySessions.createRoute(courseId)) }) {
                        Text("Sessions")
                    }
                }
            }

            // ── Exam countdown card (primary-colored per stitch design) ───────
            course.examDate?.let { examMillis ->
                item {
                    val now     = System.currentTimeMillis()
                    val days    = ((examMillis - now) / (1000L * 60 * 60 * 24)).toInt()
                    val dateStr = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
                        .format(Date(examMillis))

                    val isPast   = days < 0
                    val isUrgent = days in 0..3

                    Surface(
                        shape  = RoundedCornerShape(20.dp),
                        color  = when {
                            isPast   -> MaterialTheme.colorScheme.errorContainer
                            isUrgent -> MaterialTheme.colorScheme.error
                            else     -> MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "UPCOMING MILESTONE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        isPast -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                        else   -> Color.White.copy(alpha = 0.75f)
                                    }
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Exam Date",
                                    style     = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color     = if (isPast) MaterialTheme.colorScheme.onErrorContainer
                                                else Color.White
                                )
                                Text(
                                    dateStr,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isPast) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                            else Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    when {
                                        isPast   -> "Passed"
                                        days == 0 -> "TODAY"
                                        days == 1 -> "Tomorrow"
                                        else      -> "In $days days"
                                    },
                                    style     = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color     = if (isPast) MaterialTheme.colorScheme.onErrorContainer
                                                else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // ── Assignments section ───────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ASSIGNMENTS (${active.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = { navController.navigate(Screen.Add.route) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add task",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (courseTasks.isEmpty()) {
                item {
                    Surface(
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(16.dp),
                        color     = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Text(
                            "No tasks linked to this course yet.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(active.sortedByDescending { it.score() }) { task ->
                    CourseTaskRow(
                        task    = task,
                        onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }
                    )
                }

                // Done tasks (collapsed visual)
                if (done.isNotEmpty()) {
                    item {
                        Text(
                            "COMPLETED (${done.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    items(done) { task ->
                        CourseTaskRow(
                            task    = task,
                            onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseTaskRow(task: StudyTask, onClick: () -> Unit) {
    val score      = task.score()
    val scoreColor = scoreToRiskColorSet(score).text
    val isDone     = task.isCompleted

    Surface(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        color     = if (isDone) MaterialTheme.colorScheme.surfaceContainerLowest
                    else MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Task type icon container
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDone) MaterialTheme.colorScheme.surfaceContainerHigh
                            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ) {
                    Box(
                        modifier = Modifier.size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            if (isDone) Icons.Default.CheckCircle else Icons.Default.Assignment,
                            contentDescription = null,
                            tint     = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                                       else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        task.taskType.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Score / deadline
                Column(horizontalAlignment = Alignment.End) {
                    val dl = task.deadlineLabel()
                    if (dl.isNotEmpty() && !isDone) {
                        Text(
                            "DUE $dl".uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (task.isOverdue()) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!isDone) {
                        Text(
                            "${String.format("%.0f", score)}/100",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = scoreColor
                        )
                    }
                }
            }
            // Progress bar
            if (task.progress > 0 && !isDone) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress   = { task.progress / 100f },
                    modifier   = Modifier.fillMaxWidth().height(3.dp),
                    color      = scoreColor,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            }
        }
    }
}
