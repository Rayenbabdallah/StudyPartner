package com.example.studypartner

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studypartner.ui.theme.BookmarkGold
import com.example.studypartner.ui.theme.DeepOrange
import com.example.studypartner.ui.theme.Dimens
import com.example.studypartner.ui.theme.LimeCheck
import com.example.studypartner.ui.theme.OrangeCheck

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(navController: NavController, viewModel: StudyViewModel, taskId: Int) {

    val task = viewModel.getTaskById(taskId) ?: run {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    LaunchedEffect(taskId) { viewModel.watchSubtasksForTask(taskId) }
    val subtasks by viewModel.currentSubtasks.collectAsState()
    val haptic   = LocalHapticFeedback.current

    var newSubtaskTitle by rememberSaveable { mutableStateOf("") }

    val score              = task.score()
    val breakdown          = viewModel.scoreBreakdown(task)
    val suggestedSession   = viewModel.suggestStudySessionPlan(
        task   = task,
        course = task.courseId?.let { viewModel.getCourseById(it) }
    )

    val accent = when {
        task.isCompleted          -> LimeCheck
        task.isOverdue()          -> MaterialTheme.colorScheme.error
        task.isHighRisk()         -> DeepOrange
        score >= 60               -> OrangeCheck
        else                      -> BookmarkGold
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
                        navController.navigate(Screen.Edit.createRoute(task.id))
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    if (!task.isCompleted) {
                        IconButton(onClick = { viewModel.markAsUrgent(task) }) {
                            Icon(Icons.Default.Warning, contentDescription = "Mark urgent", tint = OrangeCheck)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.background,
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
                .padding(horizontal = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg)
        ) {

            // ── HERO ──────────────────────────────────────────────────────────
            Column(modifier = Modifier.fillMaxWidth().padding(top = Dimens.SpaceSm)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
                    Text(
                        text  = task.subject.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(Dimens.SpaceXs))
                Text(
                    text       = task.title,
                    fontSize   = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(Modifier.height(Dimens.SpaceMd))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)) {
                    RingProgress(
                        progress    = (score.toFloat() / 100f).coerceIn(0f, 1f),
                        size        = 86.dp,
                        strokeWidth = 9.dp,
                        color       = accent,
                        trackColor  = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text       = "${score.toInt()}",
                                fontSize   = 24.sp,
                                lineHeight = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "/ 100",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        DetailBadge(task.taskType.label, accent.copy(alpha = 0.14f), accent)
                        DetailBadge(task.priorityLabel(), accent.copy(alpha = 0.14f), accent)
                        if (task.deadline != null) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text  = task.deadlineLabel(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (task.isOverdue()) MaterialTheme.colorScheme.error
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                if (!task.isCompleted) {
                    Spacer(Modifier.height(Dimens.SpaceMd))
                    val animProgress by animateFloatAsState(
                        targetValue   = task.progress / 100f,
                        animationSpec = tween(800),
                        label         = "task_progress"
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Progress", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${task.progress}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        LinearProgressIndicator(
                            progress = { animProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = accent,
                            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                        )
                    }
                }
            }

            // ── STATS ROW ─────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
            ) {
                DetailStat("Difficulty", Level.fromValue(task.difficulty).label, Modifier.weight(1f))
                DetailStat("Urgency",    Level.fromValue(task.urgency).label,    Modifier.weight(1f))
                DetailStat("Impact",     task.gradeImpact.label,                 Modifier.weight(1f))
            }

            // ── SCORE BREAKDOWN ───────────────────────────────────────────────
            DetailEyebrow("PRIORITY BREAKDOWN")
            PaperPanel {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    breakdown.forEach { factor ->
                        ScoreFactorRow(factor = factor, accent = accent)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text  = "${String.format("%.1f", score)} / 100",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = accent
                        )
                    }
                }
            }

            // ── SUBTASKS ──────────────────────────────────────────────────────
            DetailEyebrow(
                "SUBTASKS",
                trailing = if (subtasks.isNotEmpty()) "${subtasks.count { it.isCompleted }} / ${subtasks.size}" else null
            )
            PaperPanel {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (subtasks.isEmpty()) {
                        Text(
                            "No subtasks yet — break this task into smaller steps below.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        subtasks.forEach { sub ->
                            PaperSubtaskRow(
                                subTask  = sub,
                                accent   = accent,
                                onToggle = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.toggleSubTask(sub)
                                },
                                onDelete = { viewModel.deleteSubTask(sub) }
                            )
                        }
                    }
                    AddSubtaskInput(
                        value     = newSubtaskTitle,
                        onChange  = { newSubtaskTitle = it },
                        onSubmit  = {
                            if (newSubtaskTitle.isNotBlank()) {
                                viewModel.addSubTask(task.id, newSubtaskTitle)
                                newSubtaskTitle = ""
                            }
                        }
                    )
                }
            }

            // ── AI SESSION ────────────────────────────────────────────────────
            DetailEyebrow("AI SESSION SUGGESTION")
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Dimens.CardPadding), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BookmarkGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF3D2400), modifier = Modifier.size(16.dp))
                        }
                        Text(
                            "${suggestedSession.durationMinutes} min focus · ${suggestedSession.breakLengthMinutes} min break / ${suggestedSession.breakEveryMinutes} min",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Text(
                        suggestedSession.suggestionReason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // ── ACTIONS ───────────────────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                Button(
                    onClick = {
                        val plan = viewModel.startSessionForTask(task.id)
                        viewModel.saveStudySession(plan = plan, name = plan.contextLabel)
                        navController.navigate(Screen.QuickFocusMode.route)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepOrange,
                        contentColor   = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Bolt, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start focus", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    OutlinedButton(
                        onClick  = {
                            navController.navigate(
                                Screen.AddStudySession.createRoute(taskId = task.id, courseId = task.courseId)
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Customize", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick  = { navController.navigate(Screen.AiTaskBreakdown.createRoute(task.id)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("AI breakdown", style = MaterialTheme.typography.labelMedium)
                    }
                }
                Button(
                    onClick = {
                        viewModel.toggleComplete(task)
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceContainer
                                         else LimeCheck,
                        contentColor   = if (task.isCompleted) MaterialTheme.colorScheme.onSurface
                                         else Color(0xFF1A2C00)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        if (task.isCompleted) Icons.Default.RadioButtonUnchecked
                        else Icons.Default.CheckCircle,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (task.isCompleted) "Mark incomplete" else "Mark complete",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(Dimens.SpaceLg))
        }
    }
}

// ─── HELPERS ─────────────────────────────────────────────────────────────────

@Composable
private fun DetailEyebrow(label: String, trailing: String? = null) {
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
        if (trailing != null) {
            Spacer(Modifier.weight(1f))
            Text(
                text  = trailing,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DetailBadge(text: String, bg: Color, fg: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg
        )
    }
}

@Composable
private fun DetailStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color    = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text  = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaperPanel(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape    = RoundedCornerShape(20.dp),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.padding(Dimens.CardPadding)) { content() }
    }
}

@Composable
private fun ScoreFactorRow(factor: PriorityEngine.ScoreFactor, accent: Color) {
    val progress by animateFloatAsState(
        targetValue   = (factor.contribution / factor.maxContribution).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(700),
        label         = "factor_${factor.label}"
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(factor.label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                String.format("%.1f", factor.contribution),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = accent,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
        )
    }
}

@Composable
private fun PaperSubtaskRow(
    subTask: SubTask,
    accent: Color,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = if (subTask.isCompleted) Icons.Default.CheckCircle
                              else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (subTask.isCompleted) LimeCheck
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text  = subTask.title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (subTask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
            textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun AddSubtaskInput(
    value: String,
    onChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        color    = MaterialTheme.colorScheme.surfaceContainer,
        shape    = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        "Add subtask…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onChange,
                    textStyle = TextStyle(
                        color    = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(DeepOrange),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(onClick = onSubmit, enabled = value.isNotBlank(), modifier = Modifier.size(32.dp)) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add",
                    tint = if (value.isNotBlank()) DeepOrange else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
