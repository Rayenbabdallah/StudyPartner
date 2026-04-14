package com.example.studypartner

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studypartner.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(navController: NavController, viewModel: StudyViewModel, taskId: Int) {

    val task = viewModel.getTaskById(taskId) ?: run {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    // Load subtasks for this task
    LaunchedEffect(taskId) { viewModel.watchSubtasksForTask(taskId) }
    val subtasks by viewModel.currentSubtasks.collectAsState()
    val haptic   = LocalHapticFeedback.current

    var newSubtaskTitle by remember { mutableStateOf("") }
    var animate         by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animate = true }

    val score      = task.score()
    val breakdown  = viewModel.scoreBreakdown(task)
    val suggestedSessionPlan = viewModel.suggestStudySessionPlan(
        task = task,
        course = task.courseId?.let { viewModel.getCourseById(it) }
    )
    val animScore  by animateFloatAsState(
        targetValue   = if (animate) (score / 100f).toFloat() else 0f,
        animationSpec = tween(900),
        label         = "score_ring"
    )

    val scoreColor = scoreToRiskColorSet(score).text

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.Edit.createRoute(task.id))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    if (!task.isCompleted) {
                        IconButton(onClick = { viewModel.markAsUrgent(task) }) {
                            Icon(Icons.Default.Warning, contentDescription = "Mark urgent",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Hero ──────────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Score ring
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                        CircularProgressIndicator(
                            progress      = { animScore },
                            modifier      = Modifier.size(80.dp),
                            strokeWidth   = 7.dp,
                            color         = scoreColor,
                            trackColor    = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap     = StrokeCap.Round
                        )
                        Text(
                            text  = String.format("%.0f", score),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold, fontSize = 22.sp),
                            color = scoreColor
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough
                                             else TextDecoration.None
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(task.subject,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TaskBadge(task.taskType.label, MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.primary)
                            val rc = riskColorSet(task.riskLabel())
                            TaskBadge(task.priorityLabel(), rc.background, rc.text)
                        }
                    }
                }

                // Progress bar
                if (!task.isCompleted) {
                    Column(modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Progress", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${task.progress}%", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        val animProgress by animateFloatAsState(
                            targetValue   = if (animate) task.progress / 100f else 0f,
                            animationSpec = tween(800),
                            label         = "task_progress"
                        )
                        LinearProgressIndicator(
                            progress   = { animProgress },
                            modifier   = Modifier.fillMaxWidth().height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color      = scoreColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // ── Info row ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoCard("Difficulty", Level.fromValue(task.difficulty).label, Modifier.weight(1f))
                InfoCard("Urgency",    Level.fromValue(task.urgency).label,    Modifier.weight(1f))
                InfoCard("Impact",     task.gradeImpact.label,                 Modifier.weight(1f))
            }

            if (task.deadline != null) {
                val dl = task.deadlineLabel()
                InfoCard(
                    label = "Deadline",
                    value = dl,
                    modifier = Modifier.fillMaxWidth(),
                    valueColor = if (task.isOverdue()) MaterialTheme.colorScheme.error
                                 else MaterialTheme.colorScheme.onSurface
                )
            }

            // ── Score breakdown ───────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Score Breakdown",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary)
                    breakdown.forEach { factor ->
                        ScoreFactorRow(factor = factor, animate = animate, scoreColor = scoreColor)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface)
                        Text("${String.format("%.1f", score)} / 100",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = scoreColor)
                    }
                }
            }

            // ── Subtasks ──────────────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Subtasks",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary)
                        val doneCount = subtasks.count { it.isCompleted }
                        if (subtasks.isNotEmpty()) {
                            Text("$doneCount / ${subtasks.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    if (subtasks.isEmpty()) {
                        Text("No subtasks yet — break this task down.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        subtasks.forEach { sub ->
                            SubtaskRow(
                                subTask  = sub,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleSubTask(sub)
                                },
                                onDelete = { viewModel.deleteSubTask(sub) }
                            )
                        }
                    }

                    // Add subtask input
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value         = newSubtaskTitle,
                            onValueChange = { newSubtaskTitle = it },
                            placeholder   = { Text("Add subtask…") },
                            singleLine    = true,
                            modifier      = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                viewModel.addSubTask(task.id, newSubtaskTitle)
                                newSubtaskTitle = ""
                            })
                        )
                        IconButton(
                            onClick = {
                                viewModel.addSubTask(task.id, newSubtaskTitle)
                                newSubtaskTitle = ""
                            },
                            enabled = newSubtaskTitle.isNotBlank()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add",
                                tint = if (newSubtaskTitle.isNotBlank()) MaterialTheme.colorScheme.primary
                                       else MaterialTheme.colorScheme.outlineVariant)
                        }
                    }
                }
            }

            // ── Toggle complete ───────────────────────────────────────────────
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("AI Session Suggestion", fontWeight = FontWeight.SemiBold)
                    Text(
                        "${suggestedSessionPlan.durationMinutes} min focus, " +
                            "${suggestedSessionPlan.breakLengthMinutes} min break every ${suggestedSessionPlan.breakEveryMinutes} min",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        suggestedSessionPlan.suggestionReason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = {
                    val plan = viewModel.startSessionForTask(task.id)
                    viewModel.saveStudySession(plan = plan, name = plan.contextLabel)
                    navController.navigate(Screen.QuickFocusMode.route)
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                )
            ) {
                Icon(Icons.Default.Timer, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Study Session (AI Suggested)", style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = {
                    navController.navigate(
                        Screen.AddStudySession.createRoute(
                            taskId = task.id,
                            courseId = task.courseId
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Customize Session + Breaks")
            }

            OutlinedButton(
                onClick = { navController.navigate(Screen.AiTaskBreakdown.createRoute(task.id)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Task Breakdown")
            }

            Button(
                onClick = { viewModel.toggleComplete(task); navController.popBackStack() },
                modifier       = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp),
                colors         = ButtonDefaults.buttonColors(
                    containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant
                                     else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    if (task.isCompleted) Icons.Default.RadioButtonUnchecked
                    else Icons.Default.CheckCircle,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (task.isCompleted) "Mark as Incomplete" else "Mark as Complete",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TaskBadge(text: String, bg: Color, fg: Color) {
    Surface(shape = MaterialTheme.shapes.small, color = bg) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = fg)
    }
}

@Composable
private fun InfoCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = valueColor)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ScoreFactorRow(
    factor: PriorityEngine.ScoreFactor,
    animate: Boolean,
    scoreColor: Color
) {
    val progress by animateFloatAsState(
        targetValue   = if (animate) (factor.contribution / factor.maxContribution).toFloat().coerceIn(0f, 1f) else 0f,
        animationSpec = tween(700),
        label         = "factor_${factor.label}"
    )
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(factor.label,
            style    = MaterialTheme.typography.bodySmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(90.dp))
        LinearProgressIndicator(
            progress   = { progress },
            modifier   = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
            color      = scoreColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(String.format("%.1f", factor.contribution),
            style    = MaterialTheme.typography.labelSmall,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(32.dp))
    }
}

@Composable
private fun SubtaskRow(subTask: SubTask, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (subTask.isCompleted) Icons.Default.CheckCircle
                              else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (subTask.isCompleted) MaterialTheme.colorScheme.secondary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text      = subTask.title,
            style     = MaterialTheme.typography.bodyMedium,
            color     = if (subTask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough
                             else TextDecoration.None,
            modifier  = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp))
        }
    }
}
