package com.example.studypartner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

private enum class TaskFilter(val label: String) {
    ALL("All"), ACTIVE("Active"), HIGH_RISK("High Risk"), DONE("Done")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    outerPadding: PaddingValues = PaddingValues()
) {
    val state        by viewModel.uiState.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }
    val scope        = rememberCoroutineScope()
    val haptic       = LocalHapticFeedback.current
    val listState    = rememberLazyListState()

    var searchQuery  by rememberSaveable { mutableStateOf("") }
    var activeFilter by rememberSaveable { mutableStateOf(TaskFilter.ALL) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Collapse FAB text when scrolling down
    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tasks") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Add.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Task")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick   = { navController.navigate(Screen.Add.route) },
                expanded  = fabExpanded,
                icon      = { Icon(Icons.Default.Add, contentDescription = null) },
                text      = { Text("New Task", style = MaterialTheme.typography.labelLarge) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->

        when (val s = state) {
            is TaskUiState.Loading -> TaskListShimmer(
                modifier = Modifier.padding(padding)
            )

            is TaskUiState.Success -> {
                val allSorted = viewModel.sortedTasks()
                val filtered  = allSorted.filter { task ->
                    val matchSearch = searchQuery.isBlank() ||
                        task.title.contains(searchQuery, ignoreCase = true) ||
                        task.subject.contains(searchQuery, ignoreCase = true)
                    val matchFilter = when (activeFilter) {
                        TaskFilter.ALL       -> true
                        TaskFilter.ACTIVE    -> !task.isCompleted
                        TaskFilter.HIGH_RISK -> task.isHighRisk() && !task.isCompleted
                        TaskFilter.DONE      -> task.isCompleted
                    }
                    matchSearch && matchFilter
                }

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh    = {
                        isRefreshing = true
                        viewModel.fetchOllamaAdvice()
                        scope.launch {
                            kotlinx.coroutines.delay(1200)
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier
                        .padding(top = padding.calculateTopPadding())
                        .fillMaxSize()
                ) {
                    Column(Modifier.fillMaxSize()) {
                        FilledTonalButton(
                            onClick = { navController.navigate(Screen.Add.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Task")
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = { navController.navigate(Screen.TodayTasks.route) },
                                label = { Text("Today") }
                            )
                            AssistChip(
                                onClick = { navController.navigate(Screen.UpcomingTasks.route) },
                                label = { Text("Upcoming") }
                            )
                            AssistChip(
                                onClick = { navController.navigate(Screen.OverdueTasks.route) },
                                label = { Text("Overdue") }
                            )
                            AssistChip(
                                onClick = { navController.navigate(Screen.CompletedTasks.route) },
                                label = { Text("Completed") }
                            )
                            AssistChip(
                                onClick = { navController.navigate(Screen.Subtasks.route) },
                                label = { Text("Subtasks") }
                            )
                            AssistChip(
                                onClick = { navController.navigate(Screen.FilterTasks.route) },
                                label = { Text("Filter") }
                            )
                        }

                        // ── Search bar ────────────────────────────────────────
                        SearchBar(
                            inputField = {
                                SearchBarDefaults.InputField(
                                    query          = searchQuery,
                                    onQueryChange  = { searchQuery = it },
                                    onSearch       = {},
                                    expanded       = false,
                                    onExpandedChange = {},
                                    placeholder    = { Text("Search tasks…") },
                                    leadingIcon    = { Icon(Icons.Default.Search, contentDescription = null) },
                                    trailingIcon   = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear")
                                            }
                                        }
                                    }
                                )
                            },
                            expanded       = false,
                            onExpandedChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {}

                        // ── Filter chips ──────────────────────────────────────
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TaskFilter.entries.forEach { filter ->
                                FilterChip(
                                    selected = activeFilter == filter,
                                    onClick  = { activeFilter = filter },
                                    label    = { Text(filter.label, style = MaterialTheme.typography.labelMedium) }
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        if (filtered.isEmpty()) {
                            EmptyStateView(
                                icon        = if (searchQuery.isNotEmpty()) Icons.Default.Search
                                              else Icons.Default.CheckCircle,
                                title       = if (searchQuery.isNotEmpty()) "No results"
                                              else "No tasks here yet",
                                subtitle    = if (searchQuery.isNotEmpty())
                                                  "No tasks match \"$searchQuery\""
                                              else "Tap 'New Task' to add your first task",
                                actionLabel = if (searchQuery.isNotEmpty()) null else "Add Task",
                                onAction    = if (searchQuery.isNotEmpty()) null else {
                                    { navController.navigate(Screen.Add.route) }
                                }
                            )
                        } else {
                            LazyColumn(
                                state          = listState,
                                contentPadding = PaddingValues(
                                    start  = 16.dp, end = 16.dp,
                                    top    = 8.dp,
                                    bottom = outerPadding.calculateBottomPadding() + 96.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filtered, key = { it.id }) { task ->
                                    SwipeToDeleteCard(
                                        modifier = Modifier.animateItem(tween(300)),
                                        onDelete = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.deleteTask(task)
                                            scope.launch {
                                                val result = snackbarHost.showSnackbar(
                                                    message     = "\"${task.title}\" deleted",
                                                    actionLabel = "Undo",
                                                    duration    = SnackbarDuration.Short
                                                )
                                                if (result == SnackbarResult.ActionPerformed) {
                                                    viewModel.undoDelete()
                                                }
                                            }
                                        }
                                    ) {
                                        TaskCard(
                                            task         = task,
                                            onMarkUrgent = { viewModel.markAsUrgent(task) },
                                            onToggleDone = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.toggleComplete(task)
                                            },
                                            onEdit = {
                                                navController.navigate(
                                                    Screen.TaskDetail.createRoute(task.id))
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is TaskUiState.Error -> ErrorState(
                message = s.message,
                onRetry = { viewModel.fetchOllamaAdvice() },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun TaskListShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBox(Modifier.fillMaxWidth(), height = 52.dp)
        ShimmerBox(Modifier.fillMaxWidth(0.6f), height = 32.dp)
        repeat(5) {
            ShimmerBox(Modifier.fillMaxWidth(), height = 90.dp)
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier          = modifier.fillMaxSize(),
        contentAlignment  = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.Warning, contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint     = MaterialTheme.colorScheme.error
            )
            Text(
                "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteCard(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange  = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        },
        positionalThreshold = { it * 0.4f }
    )
    SwipeToDismissBox(
        state                      = dismissState,
        modifier                   = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue   = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                label         = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = MaterialTheme.shapes.medium)
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    ) { content() }
}

@Composable
private fun TaskCard(
    task: StudyTask,
    modifier: Modifier = Modifier,
    onMarkUrgent: () -> Unit,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit
) {
    val done        = task.isCompleted
    val riskColors  = riskColorSet(task.riskLabel())

    val accentColor = when {
        done -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        else -> riskColors.text
    }
    val cardBg by animateColorAsState(
        targetValue   = when {
            done -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else -> riskColors.background
        },
        animationSpec = tween(400),
        label         = "cardBg"
    )

    Card(
        onClick   = { if (!done) onEdit() },
        modifier  = modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (done) 0.dp else 2.dp),
        shape     = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {

            // Accent strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggle done
                IconButton(onClick = onToggleDone, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector        = if (done) Icons.Default.CheckCircle
                                            else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (done) "Mark incomplete" else "Mark complete",
                        tint               = if (done) accentColor
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier           = Modifier.size(22.dp)
                    )
                }

                Spacer(Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text           = task.title,
                        style          = MaterialTheme.typography.titleSmall,
                        color          = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                                         else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (done) TextDecoration.LineThrough
                                         else TextDecoration.None,
                        maxLines       = 2
                    )
                    Text(
                        text     = "${task.subject} · ${task.taskType.label}",
                        style    = MaterialTheme.typography.bodySmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    if (!done) {
                        Spacer(Modifier.height(6.dp))

                        Row(
                            verticalAlignment      = Alignment.CenterVertically,
                            horizontalArrangement  = Arrangement.spacedBy(8.dp)
                        ) {
                            // Risk badge
                            Surface(
                                color    = accentColor.copy(alpha = 0.15f),
                                shape    = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text     = task.riskLabel(),
                                    style    = MaterialTheme.typography.labelSmall,
                                    color    = accentColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            // Deadline
                            if (task.deadline != null) {
                                val days = task.daysUntilDeadline()
                                val deadlineColor = when {
                                    task.isOverdue()          -> MaterialTheme.colorScheme.error
                                    days != null && days <= 1 -> MaterialTheme.colorScheme.error
                                    days != null && days <= 3 -> riskColorSet("Moderate").text
                                    else                      -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                Row(
                                    verticalAlignment     = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Schedule, contentDescription = null,
                                        modifier = Modifier.size(11.dp),
                                        tint     = deadlineColor
                                    )
                                    Text(
                                        text  = task.deadlineLabel(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = deadlineColor
                                    )
                                }
                            }
                        }

                        if (task.progress > 0) {
                            Spacer(Modifier.height(6.dp))
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LinearProgressIndicator(
                                    progress   = { task.progress / 100f },
                                    modifier   = Modifier.weight(1f).height(5.dp),
                                    color      = accentColor,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text(
                                    "${task.progress}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (!done && task.urgency < Level.EXTREME.value) {
                    IconButton(onClick = onMarkUrgent, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Warning, contentDescription = "Mark urgent",
                            tint     = riskColorSet("Moderate").text,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

