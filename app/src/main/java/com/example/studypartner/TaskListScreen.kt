package com.example.studypartner

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studypartner.ui.theme.BookmarkGold
import com.example.studypartner.ui.theme.DeepOrange
import com.example.studypartner.ui.theme.Dimens
import com.example.studypartner.ui.theme.LimeCheck
import com.example.studypartner.ui.theme.OrangeCheck
import com.example.studypartner.ui.theme.WarmAmber
import kotlinx.coroutines.launch

private enum class TaskFilter(val label: String, val accent: Color) {
    ALL("All", DeepOrange),
    ACTIVE("Active", WarmAmber),
    HIGH_RISK("Risk", OrangeCheck),
    DONE("Done", LimeCheck),
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Tasks", fontWeight = FontWeight.SemiBold) },
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
        snackbarHost = { SnackbarHost(snackbarHost) },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->

        when (val s = state) {
            is TaskUiState.Loading -> TaskListShimmer(modifier = Modifier.padding(padding))

            is TaskUiState.Success -> {
                val allSorted = remember(s.tasks) { viewModel.sortedTasks() }
                val filtered = remember(allSorted, searchQuery, activeFilter) {
                    allSorted.filter { task ->
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
                }

                val totalActive = remember(s.tasks) { s.tasks.count { !it.isCompleted } }
                val totalDone   = remember(s.tasks) { s.tasks.count { it.isCompleted } }
                val totalRisk   = remember(s.tasks) { s.tasks.count { !it.isCompleted && it.isHighRisk() } }

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
                    LazyColumn(
                        state          = listState,
                        contentPadding = PaddingValues(
                            start  = Dimens.ScreenPadding,
                            end    = Dimens.ScreenPadding,
                            top    = Dimens.SpaceXs,
                            bottom = outerPadding.calculateBottomPadding() + 96.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
                    ) {
                        item("hero") {
                            HeroHeader(
                                active   = totalActive,
                                done     = totalDone,
                                atRisk   = totalRisk,
                                onTodayClick    = { navController.navigate(Screen.TodayTasks.route) },
                                onOverdueClick  = { navController.navigate(Screen.OverdueTasks.route) }
                            )
                        }
                        item("search") {
                            PaperSearchField(
                                value = searchQuery,
                                onChange = { searchQuery = it },
                                modifier = Modifier.padding(top = Dimens.SpaceXs)
                            )
                        }
                        item("filters") {
                            FilterTabs(
                                selected   = activeFilter,
                                onSelect   = { activeFilter = it }
                            )
                        }

                        if (filtered.isEmpty()) {
                            item("empty") {
                                EmptyStateView(
                                    icon        = if (searchQuery.isNotEmpty()) Icons.Default.Search
                                                  else Icons.Default.CheckCircle,
                                    title       = if (searchQuery.isNotEmpty()) "No results"
                                                  else "Nothing here yet",
                                    subtitle    = if (searchQuery.isNotEmpty())
                                                      "No tasks match \"$searchQuery\""
                                                  else "Tap 'New task' below to get started.",
                                    actionLabel = if (searchQuery.isNotEmpty()) null else "New task",
                                    onAction    = if (searchQuery.isNotEmpty()) null else {
                                        { navController.navigate(Screen.Add.route) }
                                    }
                                )
                            }
                        } else {
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
                                    PaperTaskCard(
                                        task         = task,
                                        onMarkUrgent = { viewModel.markAsUrgent(task) },
                                        onToggleDone = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            viewModel.toggleComplete(task)
                                        },
                                        onEdit = {
                                            navController.navigate(Screen.TaskDetail.createRoute(task.id))
                                        }
                                    )
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

// ─── HERO ────────────────────────────────────────────────────────────────────

@Composable
private fun HeroHeader(
    active: Int,
    done: Int,
    atRisk: Int,
    onTodayClick: () -> Unit,
    onOverdueClick: () -> Unit
) {
    Column(modifier = Modifier.padding(top = Dimens.SpaceSm)) {
        Text(
            text  = "YOUR TASKS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(Dimens.SpaceXs))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text       = "$active",
                fontSize   = 72.sp,
                lineHeight = 76.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.width(Dimens.SpaceMd))
            Column(
                modifier = Modifier.padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "active",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "$done done · $atRisk at risk",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(Dimens.SpaceMd))
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
            CategoryPill("Today",    DeepOrange,    onTodayClick)
            CategoryPill("Overdue",  MaterialTheme.colorScheme.error, onOverdueClick)
        }
    }
}

@Composable
private fun CategoryPill(label: String, accent: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color    = MaterialTheme.colorScheme.surfaceContainer,
        shape    = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─── SEARCH ──────────────────────────────────────────────────────────────────

@Composable
private fun PaperSearchField(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape    = RoundedCornerShape(14.dp),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        "Search tasks…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onChange,
                    textStyle = TextStyle(
                        color    = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    ),
                    cursorBrush = SolidColor(DeepOrange),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (value.isNotEmpty()) {
                IconButton(onClick = { onChange("") }, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ─── FILTER TABS ─────────────────────────────────────────────────────────────

@Composable
private fun FilterTabs(
    selected: TaskFilter,
    onSelect: (TaskFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
    ) {
        TaskFilter.entries.forEach { filter ->
            FilterTab(
                label    = filter.label,
                accent   = filter.accent,
                selected = filter == selected,
                onClick  = { onSelect(filter) }
            )
        }
    }
}

@Composable
private fun FilterTab(
    label: String,
    accent: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (selected) accent else MaterialTheme.colorScheme.surfaceContainer,
        label = "tabBg"
    )
    val fg by animateColorAsState(
        targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
        label = "tabFg"
    )
    Surface(
        modifier = Modifier.clickable { onClick() },
        color    = bg,
        shape    = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text  = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = fg
            )
        }
    }
}

// ─── TASK CARD ───────────────────────────────────────────────────────────────

@Composable
private fun PaperTaskCard(
    task: StudyTask,
    onMarkUrgent: () -> Unit,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit
) {
    val done       = task.isCompleted
    val riskColors = riskColorSet(task.riskLabel())
    val accent     = when {
        done                       -> LimeCheck
        task.isOverdue()           -> MaterialTheme.colorScheme.error
        task.isHighRisk()          -> DeepOrange
        else                       -> riskColors.text
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { if (!done) onEdit() },
        color    = if (done) MaterialTheme.colorScheme.surfaceContainerLow
                   else MaterialTheme.colorScheme.surfaceContainerLowest,
        shape    = RoundedCornerShape(16.dp),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            // Left accent strip
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleDone, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (done) Icons.Default.CheckCircle
                                     else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (done) "Mark incomplete" else "Mark complete",
                        tint = if (done) LimeCheck else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (done) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text  = "${task.subject} · ${task.taskType.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )

                    if (!done) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = accent.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text  = task.riskLabel(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = accent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            if (task.deadline != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text  = task.deadlineLabel(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            Text(
                                text  = "${task.score().toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (task.progress > 0) {
                            Spacer(Modifier.height(8.dp))
                            val animProgress by animateFloatAsState(
                                targetValue = task.progress / 100f,
                                animationSpec = tween(600),
                                label = "progress"
                            )
                            LinearProgressIndicator(
                                progress = { animProgress },
                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                color = accent,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }
                    }
                }
                if (!done && task.urgency < Level.EXTREME.value) {
                    IconButton(onClick = onMarkUrgent, modifier = Modifier.size(34.dp)) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Mark urgent",
                            tint = OrangeCheck,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─── SHIMMER / ERROR ─────────────────────────────────────────────────────────

@Composable
private fun TaskListShimmer(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpaceLg),
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
    ) {
        ShimmerBox(Modifier.fillMaxWidth(0.4f), height = 18.dp)
        ShimmerBox(Modifier.fillMaxWidth(0.5f), height = 56.dp)
        ShimmerBox(Modifier.fillMaxWidth(), height = 44.dp)
        repeat(5) { ShimmerBox(Modifier.fillMaxWidth(), height = 84.dp) }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd),
            modifier = Modifier.padding(Dimens.SpaceXl)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepOrange,
                    contentColor   = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Retry") }
        }
    }
}

// ─── SWIPE TO DELETE ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteCard(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
        },
        positionalThreshold = { it * 0.4f }
    )
    SwipeToDismissBox(
        state                       = dismissState,
        modifier                    = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart)
                    MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, shape = RoundedCornerShape(16.dp))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    ) { content() }
}
