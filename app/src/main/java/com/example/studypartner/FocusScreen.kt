package com.example.studypartner

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

private const val DEFAULT_FOCUS_SECONDS = 25 * 60

@Composable
fun FocusScreen(navController: NavController, viewModel: StudyViewModel, taskId: Int) {

    val task     = viewModel.getTaskById(taskId)
    val strings  = AppStrings.get(LocalTunisianMode.current)
    val mvw      = task?.let { viewModel.minimumViableWork(it) } ?: ""

    var totalSeconds by remember { mutableIntStateOf(DEFAULT_FOCUS_SECONDS) }
    var secondsLeft  by remember { mutableIntStateOf(totalSeconds) }
    var running      by remember { mutableStateOf(false) }
    var finished     by remember { mutableStateOf(false) }

    val motivations     = listOf(strings.focus1, strings.focus2, strings.focus3)
    var motivationIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(running, secondsLeft) {
        if (running && secondsLeft > 0) {
            delay(1_000)
            secondsLeft -= 1
            if (secondsLeft == 0) { running = false; finished = true }
        }
    }
    LaunchedEffect(running) {
        while (running) {
            delay(30_000)
            motivationIndex = (motivationIndex + 1) % motivations.size
        }
    }

    BackHandler { navController.popBackStack() }

    val progress = if (totalSeconds > 0) secondsLeft.toFloat() / totalSeconds else 0f

    val timerColor by animateColorAsState(
        targetValue = when {
            finished        -> MaterialTheme.colorScheme.secondary
            progress > 0.5f -> MaterialTheme.colorScheme.primary
            progress > 0.2f -> MaterialTheme.colorScheme.tertiary
            else            -> MaterialTheme.colorScheme.error
        },
        animationSpec = tween(600),
        label = "timerColor"
    )

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60

    // Ambient radial gradient background (stitch "ambient-gradient")
    val ambientBg = Brush.radialGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceContainerLow
        )
    )

    if (finished) {
        // ── Session Mastered screen ───────────────────────────────────────────
        SessionMasteredScreen(
            focusMinutes = totalSeconds / 60,
            task = task,
            onBack = {
                task?.let { viewModel.setProgress(it, 100) }
                navController.popBackStack()
            },
            onDone = {
                task?.let { t ->
                    val newProgress = (t.progress + 25).coerceAtMost(100)
                    viewModel.setProgress(t, newProgress)
                }
                navController.popBackStack()
            }
        )
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ambientBg)
    ) {
        // ── Decorative blobs ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset((-40).dp, (-40).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.BottomEnd)
                .offset(30.dp, 30.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(16.dp))

            // ── Header ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        "FOCUSING SESSION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        "StudyPartner",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                // Glass bolt icon
                Surface(
                    shape  = RoundedCornerShape(12.dp),
                    color  = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.4f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.FlashOn,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Task type chip ────────────────────────────────────────────────
            task?.let { t ->
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Assignment,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            t.taskType.label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Task title
                Text(
                    text      = t.title,
                    style     = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color     = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = "Focusing for ${totalSeconds / 60} minutes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(24.dp))
            }

            // ── Massive timer ─────────────────────────────────────────────────
            AnimatedContent(
                targetState   = "%02d:%02d".format(minutes, seconds),
                transitionSpec = { fadeIn(tween(120)) togetherWith fadeOut(tween(120)) },
                label         = "timer"
            ) { time ->
                Text(
                    text      = time,
                    fontSize  = 80.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color     = timerColor,
                    letterSpacing = (-2).sp,
                    lineHeight  = 80.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Thin progress bar ─────────────────────────────────────────────
            LinearProgressIndicator(
                progress   = { progress },
                modifier   = Modifier
                    .fillMaxWidth(0.75f)
                    .height(4.dp)
                    .clip(CircleShape),
                color      = timerColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            Spacer(Modifier.height(24.dp))

            // ── Duration adjust (only when not running) ───────────────────────
            AnimatedVisibility(visible = !running) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            totalSeconds = (totalSeconds - 5 * 60).coerceAtLeast(5 * 60)
                            secondsLeft  = totalSeconds
                        }
                    ) { Text("−5 min", style = MaterialTheme.typography.labelLarge) }

                    Text(
                        "${totalSeconds / 60} min",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = {
                            totalSeconds = (totalSeconds + 5 * 60).coerceAtMost(120 * 60)
                            secondsLeft  = totalSeconds
                        }
                    ) { Text("+5 min", style = MaterialTheme.typography.labelLarge) }
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Motivation card ───────────────────────────────────────────────
            AnimatedContent(
                targetState    = motivationIndex,
                transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                label          = "motivation"
            ) { idx ->
                Text(
                    text      = motivations[idx],
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.fillMaxWidth(0.85f)
                )
            }

            // ── MVW tip ───────────────────────────────────────────────────────
            if (mvw.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Surface(
                    color  = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape  = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text     = "💡 $mvw",
                        modifier = Modifier.padding(12.dp),
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Controls ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // PAUSE / RESET button
                Surface(
                    onClick = {
                        if (running) {
                            running = false
                        } else {
                            secondsLeft = totalSeconds
                            finished    = false
                        }
                    },
                    modifier  = Modifier
                        .weight(1f)
                        .height(54.dp),
                    shape     = RoundedCornerShape(14.dp),
                    color     = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (running) Icons.Default.Pause else Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                if (running) "PAUSE" else "RESET",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // FINISH SESSION / START button
                Button(
                    onClick = {
                        if (running || task == null) {
                            // Mark progress and go back
                            task?.let { t ->
                                val newProgress = (t.progress + 25).coerceAtMost(100)
                                viewModel.setProgress(t, newProgress)
                            }
                            navController.popBackStack()
                        } else {
                            running = true
                        }
                    },
                    modifier       = Modifier
                        .weight(1.6f)
                        .height(54.dp),
                    shape          = RoundedCornerShape(14.dp),
                    colors         = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        if (running) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (running) "FINISH SESSION" else "START SESSION",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }

        // ── Distraction Shield indicator ──────────────────────────────────────
        if (running) {
            Surface(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopStart),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    )
                    Text(
                        "DISTRACTION SHIELD ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionMasteredScreen(
    focusMinutes: Int,
    task: StudyTask?,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .systemBarsPadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Eyebrow + Headline ────────────────────────────────────────────
            Text(
                "DEEP WORK ACCOMPLISHED",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Session\nMastered.",
                style     = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                lineHeight = 44.sp,
                color     = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "The studio environment remains undisturbed. Your focus has yielded tangible progress toward academic mastery.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // ── Bento stats grid ──────────────────────────────────────────────
            // Focus Time — primary-container highlight
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape  = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            "FOCUS TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            "${focusMinutes}:00",
                            style     = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color     = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Task name / type tile
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
                    shape  = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "TASK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                task?.taskType?.label ?: "Study",
                                style     = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color     = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                // Streak tile
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(130.dp),
                    shape  = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Signature gold gradient circle
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.primaryContainer
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                "STREAK",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Keep going!",
                                style     = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color     = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            // ── CTA Buttons ───────────────────────────────────────────────────
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null,
                    modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "MARK COMPLETE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick   = onDone,
                modifier  = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape     = RoundedCornerShape(14.dp),
                colors    = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border    = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text(
                    "BACK TO DASHBOARD",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
