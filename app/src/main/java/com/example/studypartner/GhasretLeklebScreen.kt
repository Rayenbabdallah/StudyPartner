package com.example.studypartner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GhasretLeklebScreen(navController: NavController, viewModel: StudyViewModel) {

    val strings    = AppStrings.get(LocalTunisianMode.current)
    val panicTasks = viewModel.panicTasks()
    val rescuePlan by viewModel.rescuePlanState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        viewModel.dismissPanic()
                        navController.popBackStack()
                    }) {
                        Text(
                            "DISMISS",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->

        // Background urgency blobs
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset(150.dp, (-50).dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.error.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomStart)
                    .offset((-30).dp, 30.dp)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )

            LazyColumn(
                contentPadding = PaddingValues(
                    start  = 24.dp, end = 24.dp,
                    top    = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // ── Editorial header ──────────────────────────────────────────
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "URGENT: DEADLINE IMPENDING",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    buildAnnotatedStringWithSpan(
                                        normal = strings.modeName.substringBeforeLast(" ") + " ",
                                        highlighted = strings.modeName.substringAfterLast(" ")
                                    ),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            // Time remaining card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shadowElevation = 0.dp,
                                tonalElevation  = 0.dp
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        "TIME REMAINING",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        panicTasks.firstOrNull()?.let { task ->
                                            task.deadline?.let { ms ->
                                                val h = ((ms - System.currentTimeMillis()) / 3_600_000).coerceAtLeast(0)
                                                val m = (((ms - System.currentTimeMillis()) % 3_600_000) / 60_000).coerceAtLeast(0)
                                                "%02d:%02d".format(h, m)
                                            }
                                        } ?: "--:--",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                        Text(
                            "${panicTasks.size} critical task(s) require your immediate attention. Take a deep breath.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // ── Rescue plan card ──────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { navController.navigate(Screen.SaveMePlan.route) },
                            label = { Text("Save Me") }
                        )
                        AssistChip(
                            onClick = { navController.navigate(Screen.TopPriorityTask.route) },
                            label = { Text("Top Priority") }
                        )
                        AssistChip(
                            onClick = { navController.navigate(Screen.DoNowTasks.route) },
                            label = { Text("Do Now") }
                        )
                        AssistChip(
                            onClick = { navController.navigate(Screen.SkipThese.route) },
                            label = { Text("Skip These") }
                        )
                        AssistChip(
                            onClick = { navController.navigate(Screen.TimeBasedRescue.route) },
                            label = { Text("Timeline") }
                        )
                    }
                }

                item {
                    RescuePlanCard(
                        state     = rescuePlan,
                        viewModel = viewModel,
                        strings   = strings
                    )
                }

                // ── Critical tasks section ────────────────────────────────────
                item {
                    Text(
                        "SURVIVAL PRIORITY",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (panicTasks.isEmpty()) {
                    item {
                        Surface(
                            shape  = RoundedCornerShape(16.dp),
                            color  = MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "No panic tasks right now — you're doing fine!",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(panicTasks) { task ->
                        SurvivalTaskCard(
                            task      = task,
                            viewModel = viewModel,
                            strings   = strings,
                            onFocus   = { navController.navigate(Screen.Focus.createRoute(task.id)) }
                        )
                    }
                }

                // ── Glassmorphism survival tip ────────────────────────────────
                item {
                    GlassTipCard()
                }

                // ── Exit button ───────────────────────────────────────────────
                item {
                    TextButton(
                        onClick = {
                            viewModel.dismissPanic()
                            navController.popBackStack()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Dismiss and return to dashboard",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun buildAnnotatedStringWithSpan(
    normal: String,
    highlighted: String
): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        append(normal)
        pushStyle(
            androidx.compose.ui.text.SpanStyle(color = MaterialTheme.colorScheme.error)
        )
        append(highlighted)
        pop()
    }
}

@Composable
private fun RescuePlanCard(
    state: AiState,
    viewModel: StudyViewModel,
    strings: StringSet
) {
    val steps = listOf(
        "Prioritize Core Sections" to "Focus 80% of remaining energy on highest-weight parts first.",
        "AI Synthesis Help" to "Let the AI polish your existing work for flow and clarity.",
        "Final Review Pass" to "Quick check for obvious errors — secure every available mark."
    )

    Surface(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        color     = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.FlashOn,
                    contentDescription = null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "The 'Save Me' Rescue Plan",
                    style     = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color     = MaterialTheme.colorScheme.onSurface
                )
            }

            when (state) {
                is AiState.Success -> {
                    Text(
                        state.response,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { viewModel.clearRescuePlan() }) {
                        Text("Regenerate", color = MaterialTheme.colorScheme.primary)
                    }
                }
                is AiState.Loading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        steps.forEachIndexed { i, (title, _) ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier    = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color       = MaterialTheme.colorScheme.primary
                                    )
                                }
                                ShimmerBox(Modifier.fillMaxWidth(), height = 14.dp)
                            }
                        }
                    }
                }
                is AiState.Failure -> {
                    // Show local fallback with error detail
                    steps.forEachIndexed { i, (title, desc) ->
                        RescueStep(number = i + 1, title = title, description = desc)
                    }
                    Text(
                        "OpenRouter Error: ${state.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        viewModel.getLocalRescuePlan(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { viewModel.clearRescuePlan() }) {
                        Text("Retry AI", color = MaterialTheme.colorScheme.primary)
                    }
                }
                is AiState.Unavailable -> {
                    // Show local numbered steps
                    steps.forEachIndexed { i, (title, desc) ->
                        RescueStep(number = i + 1, title = title, description = desc)
                    }
                    Text(
                        viewModel.getLocalRescuePlan(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = { viewModel.clearRescuePlan() }) {
                        Text("Retry AI", color = MaterialTheme.colorScheme.primary)
                    }
                }
                is AiState.Idle -> {
                    // Show local numbered steps
                    steps.forEachIndexed { i, (title, desc) ->
                        RescueStep(number = i + 1, title = title, description = desc)
                    }
                    Spacer(Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.fetchRescuePlan() },
                        modifier = Modifier.fillMaxWidth(),
                        shape   = RoundedCornerShape(12.dp),
                        colors  = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            strings.rescueCta.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RescueStep(number: Int, title: String, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$number",
                style     = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color     = MaterialTheme.colorScheme.primary
            )
        }
        Column {
            Text(
                title,
                style     = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color     = MaterialTheme.colorScheme.onSurface
            )
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun GlassTipCard() {
    // Glassmorphism: semi-transparent surface with soft border
    Surface(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        color     = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f),
        border    = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Coffee,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Survival Tip",
                style     = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color     = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "Drink a glass of water now. High cortisol from deadline stress dehydrates the brain faster than exercise. Clear head = better output.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SurvivalTaskCard(
    task: StudyTask,
    viewModel: StudyViewModel,
    strings: StringSet,
    onFocus: () -> Unit
) {
    val label = viewModel.survivalLabel(task, strings)
    val score = viewModel.survivalScore(task)

    val (labelColor, cardBg) = when (label) {
        strings.doNow  -> riskColorSet("High Risk").text to riskColorSet("High Risk").background
        strings.ifTime -> riskColorSet("Moderate").text  to riskColorSet("Moderate").background
        else           -> MaterialTheme.colorScheme.onSurfaceVariant to MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        color    = cardBg
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Label chip
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = labelColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        label.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style    = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color    = labelColor
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    task.title,
                    style     = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier  = Modifier.weight(1f),
                    color     = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "${String.format("%.0f", score)}/100",
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val dl = task.deadlineLabel()
                if (dl.isNotEmpty()) {
                    Text(
                        dl,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (task.isOverdue()) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${task.progress}% done",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    task.taskType.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // MVW tip
            Text(
                "💡 ${viewModel.minimumViableWork(task)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Focus button only for DO NOW
            if (label == strings.doNow) {
                Button(
                    onClick = onFocus,
                    modifier = Modifier.fillMaxWidth(),
                    shape   = RoundedCornerShape(10.dp),
                    colors  = ButtonDefaults.buttonColors(
                        containerColor = labelColor,
                        contentColor   = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(Icons.Default.Timer, contentDescription = null,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "START FOCUS SESSION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
