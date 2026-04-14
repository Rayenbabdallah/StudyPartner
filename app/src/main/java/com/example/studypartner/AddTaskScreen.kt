package com.example.studypartner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(navController: NavController, viewModel: StudyViewModel) {

    val courses       by viewModel.allCourses.collectAsState()
    var showTitleError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var courseExpanded by remember { mutableStateOf(false) }
    var isSaving       by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.prepareNewTaskForm()
    }

    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deadlineMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Task") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Task Details section ──────────────────────────────────────────
            FormSection(label = "Task Details") {

                OutlinedTextField(
                    value         = viewModel.title,
                    onValueChange = { viewModel.title = it; showTitleError = false },
                    label         = { Text("Title") },
                    isError       = showTitleError,
                    supportingText = if (showTitleError) ({ Text("Title is required") }) else null,
                    modifier       = Modifier.fillMaxWidth(),
                    singleLine     = true,
                    shape          = RoundedCornerShape(12.dp)
                )

                AnimatedVisibility(
                    visible = courses.isNotEmpty(),
                    enter   = expandVertically(),
                    exit    = shrinkVertically()
                ) {
                    ExposedDropdownMenuBox(
                        expanded         = courseExpanded,
                        onExpandedChange = { courseExpanded = it }
                    ) {
                        OutlinedTextField(
                            value         = viewModel.selectedCourse?.title ?: "No course",
                            onValueChange = {},
                            readOnly      = true,
                            label         = { Text("Course") },
                            trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                            modifier      = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape         = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded         = courseExpanded,
                            onDismissRequest = { courseExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text    = { Text("No course") },
                                onClick = { viewModel.selectedCourse = null; courseExpanded = false }
                            )
                            courses.forEach { course ->
                                DropdownMenuItem(
                                    text    = { Text(course.title) },
                                    onClick = {
                                        viewModel.selectedCourse = course
                                        viewModel.subject        = course.title
                                        viewModel.gradeImpact    = GradeImpact.fromWeight(course.creditWeight)
                                        courseExpanded           = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value         = viewModel.subject,
                    onValueChange = { viewModel.subject = it },
                    label         = { Text(if (courses.isEmpty()) "Subject / Course" else "Subject") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    enabled       = viewModel.selectedCourse == null,
                    shape         = RoundedCornerShape(12.dp)
                )

                FormEnumPicker(
                    heading  = "Type",
                    options  = TaskType.entries,
                    selected = viewModel.taskType,
                    labelOf  = { it.label },
                    onSelect = { viewModel.taskType = it }
                )

                DeadlinePicker(
                    millis  = viewModel.deadlineMillis,
                    onSet   = { showDatePicker = true },
                    onClear = { viewModel.deadlineMillis = null }
                )
            }

            // ── Priority Settings section ─────────────────────────────────────
            FormSection(label = "Priority") {

                LevelPicker(
                    label    = "Difficulty",
                    selected = viewModel.difficulty,
                    onSelect = { viewModel.difficulty = it }
                )

                LevelPicker(
                    label    = "Urgency",
                    selected = viewModel.urgency,
                    onSelect = { viewModel.urgency = it }
                )

                FormEnumPicker(
                    heading  = "Grade Impact",
                    options  = GradeImpact.entries,
                    selected = viewModel.gradeImpact,
                    labelOf  = { it.label },
                    onSelect = { viewModel.gradeImpact = it }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Save button ───────────────────────────────────────────────────
            Button(
                onClick = {
                    if (viewModel.title.isBlank()) {
                        showTitleError = true
                    } else {
                        isSaving = true
                        viewModel.addTask()
                        navController.popBackStack()
                    }
                },
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape          = RoundedCornerShape(14.dp),
                enabled        = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Task", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Shared form section container ─────────────────────────────────────────────

@Composable
internal fun FormSection(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.primary
        )
        Surface(
            color     = MaterialTheme.colorScheme.surface,
            shape     = RoundedCornerShape(16.dp),
            tonalElevation = 2.dp,
            modifier  = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier            = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content             = content
            )
        }
    }
}

// Legacy alias — keep for backward compat with EditTaskScreen
@Composable
internal fun FormSectionLabel(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.primary
    )
}

@Composable
internal fun DeadlinePicker(millis: Long?, onSet: () -> Unit, onClear: () -> Unit) {
    val label = millis?.let {
        SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(12.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.CalendarMonth, contentDescription = null,
                tint     = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Deadline", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text  = label ?: "No deadline set",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (label != null) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (millis != null) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Close, contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            TextButton(onClick = onSet) { Text(if (label != null) "Change" else "Set") }
        }
    }
}

@Composable
internal fun LevelPicker(label: String, selected: Level, onSelect: (Level) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                selected.label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Level.entries.forEach { level ->
                FilterChip(
                    selected = level == selected,
                    onClick  = { onSelect(level) },
                    label    = { Text(level.label, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun <T> FormEnumPicker(
    heading: String,
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(heading, style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                labelOf(selected),
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick  = { onSelect(option) },
                    label    = { Text(labelOf(option), style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
