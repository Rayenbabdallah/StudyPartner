package com.example.studypartner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    existingCourseId: Int? = null
) {
    val courses by viewModel.allCourses.collectAsState()
    val existing = existingCourseId?.let { id -> courses.find { it.id == id } }

    var title         by remember { mutableStateOf(existing?.title         ?: "") }
    var instructor    by remember { mutableStateOf(existing?.instructor    ?: "") }
    var creditWeight  by remember { mutableStateOf(existing?.creditWeight  ?: 0.5f) }
    var selectedColor by remember { mutableStateOf(existing?.colorHex      ?: COURSE_COLOR_PALETTE.first()) }
    var examMillis    by remember { mutableStateOf<Long?>(existing?.examDate) }
    var showTitleErr  by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = existing?.examDate)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    examMillis = datePickerState.selectedDateMillis
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
                title = { Text(if (existing == null) "New Course" else "Edit Course") },
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

            FormSectionLabel("Course Details")

            OutlinedTextField(
                value         = title,
                onValueChange = { title = it; showTitleErr = false },
                label         = { Text("Course Title") },
                isError       = showTitleErr,
                supportingText = if (showTitleErr) ({ Text("Title is required") }) else null,
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )

            OutlinedTextField(
                value         = instructor,
                onValueChange = { instructor = it },
                label         = { Text("Instructor (optional)") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )

            // ── Exam date ─────────────────────────────────────────────────────
            val examLabel = examMillis?.let {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(it))
            }
            Card(
                modifier  = Modifier.fillMaxWidth(),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Exam Date", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(examLabel ?: "Not set", style = MaterialTheme.typography.bodyMedium,
                            color = if (examLabel != null) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (examMillis != null) {
                        IconButton(onClick = { examMillis = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(if (examLabel != null) "Change" else "Set")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Credit weight (grade importance) ──────────────────────────────
            FormSectionLabel("Grade Importance")

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Credit Weight", style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        GradeImpact.fromWeight(creditWeight).label,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GradeImpact.entries.forEach { gi ->
                        FilterChip(
                            selected = GradeImpact.fromWeight(creditWeight) == gi,
                            onClick  = { creditWeight = gi.weight },
                            label    = { Text(gi.label, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Color picker ──────────────────────────────────────────────────
            FormSectionLabel("Color")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                COURSE_COLOR_PALETTE.forEach { hex ->
                    val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }
                        .getOrDefault(MaterialTheme.colorScheme.primary)
                    val isSelected = hex == selectedColor
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            )
                            .clickable { selectedColor = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null,
                                tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        showTitleErr = true
                    } else {
                        val course = Course(
                            id           = existing?.id ?: 0,
                            title        = title,
                            instructor   = instructor,
                            creditWeight = creditWeight,
                            colorHex     = selectedColor,
                            examDate     = examMillis
                        )
                        if (existing == null) viewModel.addCourse(course)
                        else viewModel.updateCourse(course)
                        navController.popBackStack()
                    }
                },
                modifier       = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (existing == null) "Save Course" else "Update Course",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
