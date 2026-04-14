package com.example.studypartner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(navController: NavController, viewModel: StudyViewModel, taskId: Int) {

    val task = viewModel.getTaskById(taskId) ?: run {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    val courses        by viewModel.allCourses.collectAsState()
    var title          by remember { mutableStateOf(task.title) }
    var subject        by remember { mutableStateOf(task.subject) }
    var selectedCourse by remember { mutableStateOf(courses.find { it.id == task.courseId }) }
    var difficulty     by remember { mutableStateOf(Level.fromValue(task.difficulty)) }
    var urgency        by remember { mutableStateOf(Level.fromValue(task.urgency)) }
    var deadlineMillis by remember { mutableStateOf<Long?>(task.deadline) }
    var taskType       by remember { mutableStateOf(TaskType.fromName(task.type)) }
    var gradeImpact    by remember { mutableStateOf(GradeImpact.fromWeight(task.gradeWeight)) }
    var progress       by remember { mutableIntStateOf(task.progress) }
    var showTitleError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var courseExpanded by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = task.deadline)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    deadlineMillis = datePickerState.selectedDateMillis
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
                title = { Text("Edit Task") },
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
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Task Details ──────────────────────────────────────────────────
            FormSectionLabel("Task Details")

            OutlinedTextField(
                value         = title,
                onValueChange = { title = it; showTitleError = false },
                label         = { Text("Title") },
                isError       = showTitleError,
                supportingText = if (showTitleError) ({ Text("Title is required") }) else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )

            // Course dropdown
            if (courses.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded         = courseExpanded,
                    onExpandedChange = { courseExpanded = it }
                ) {
                    OutlinedTextField(
                        value         = selectedCourse?.title ?: "No course",
                        onValueChange = {},
                        readOnly      = true,
                        label         = { Text("Course") },
                        trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded         = courseExpanded,
                        onDismissRequest = { courseExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text    = { Text("No course") },
                            onClick = { selectedCourse = null; courseExpanded = false }
                        )
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text    = { Text(course.title) },
                                onClick = {
                                    selectedCourse = course
                                    subject        = course.title
                                    gradeImpact    = GradeImpact.fromWeight(course.creditWeight)
                                    courseExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value         = subject,
                onValueChange = { subject = it },
                label         = { Text(if (courses.isEmpty()) "Subject / Course" else "Subject") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                enabled       = selectedCourse == null
            )

            FormEnumPicker(
                heading  = "Type",
                options  = TaskType.entries,
                selected = taskType,
                labelOf  = { it.label },
                onSelect = { taskType = it }
            )

            DeadlinePicker(
                millis  = deadlineMillis,
                onSet   = { showDatePicker = true },
                onClear = { deadlineMillis = null }
            )

            Spacer(Modifier.height(12.dp))

            // ── Priority Settings ─────────────────────────────────────────────
            FormSectionLabel("Priority Settings")

            LevelPicker(label = "Difficulty", selected = difficulty, onSelect = { difficulty = it })
            LevelPicker(label = "Urgency",    selected = urgency,    onSelect = { urgency    = it })

            FormEnumPicker(
                heading  = "Grade Impact",
                options  = GradeImpact.entries,
                selected = gradeImpact,
                labelOf  = { it.label },
                onSelect = { gradeImpact = it }
            )

            Spacer(Modifier.height(12.dp))

            // ── Progress ──────────────────────────────────────────────────────
            FormSectionLabel("Progress")

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Completion", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "$progress%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (progress >= 100) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value         = progress.toFloat(),
                    onValueChange = { progress = it.toInt() },
                    valueRange    = 0f..100f,
                    steps         = 19,
                    modifier      = Modifier.fillMaxWidth()
                )
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color    = if (progress >= 100) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        showTitleError = true
                    } else {
                        viewModel.updateTask(
                            original       = task,
                            title          = title,
                            subject        = subject,
                            courseId       = selectedCourse?.id,
                            difficulty     = difficulty,
                            urgency        = urgency,
                            deadlineMillis = deadlineMillis,
                            type           = taskType,
                            gradeImpact    = gradeImpact,
                            progress       = progress
                        )
                        navController.popBackStack()
                    }
                },
                modifier       = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Update Task", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
