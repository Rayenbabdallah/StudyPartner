package com.example.studypartner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
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
fun CoursesScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    outerPadding: PaddingValues = PaddingValues()
) {
    val courses  by viewModel.allCourses.collectAsState()
    val tasks    by viewModel.uiState.collectAsState()
    val taskList = (tasks as? TaskUiState.Success)?.tasks ?: emptyList()
    val listState = rememberLazyListState()

    val fabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Courses") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AddCourse.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Course")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = { navController.navigate(Screen.AddCourse.route) },
                expanded       = fabExpanded,
                icon           = { Icon(Icons.Default.Add, contentDescription = null) },
                text           = { Text("Add Course", style = MaterialTheme.typography.labelLarge) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->

        if (courses.isEmpty()) {
            EmptyStateView(
                icon        = Icons.Default.School,
                title       = "No courses yet",
                subtitle    = "Add your first course to link tasks and track your readiness per subject.",
                actionLabel = "Add Course",
                onAction    = { navController.navigate(Screen.AddCourse.route) },
                modifier    = Modifier
                    .padding(padding)
                    .padding(bottom = outerPadding.calculateBottomPadding())
            )
        } else {
            LazyColumn(
                state          = listState,
                contentPadding = PaddingValues(
                    start  = 16.dp, end = 16.dp,
                    top    = padding.calculateTopPadding() + 8.dp,
                    bottom = outerPadding.calculateBottomPadding() + 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    FilledTonalButton(
                        onClick = { navController.navigate(Screen.AddCourse.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Add Course")
                    }
                }
                items(courses, key = { it.id }) { course ->
                    val courseTasks = taskList.filter { it.courseId == course.id }
                    CourseCard(
                        course         = course,
                        activeCount    = courseTasks.count { !it.isCompleted },
                        riskScore      = viewModel.courseRiskScore(course),
                        readinessScore = viewModel.studyReadinessScore(course),
                        onClick        = { navController.navigate(Screen.CourseDetail.createRoute(course.id)) },
                        onEdit         = { navController.navigate(Screen.EditCourse.createRoute(course.id)) },
                        onDelete       = { viewModel.deleteCourse(course) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseCard(
    course: Course,
    activeCount: Int,
    riskScore: Double,
    readinessScore: Int,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val courseColor = runCatching {
        Color(android.graphics.Color.parseColor(course.colorHex))
    }.getOrDefault(MaterialTheme.colorScheme.primary)

    val riskColors     = scoreToRiskColorSet(riskScore)
    val readinessLabel = when {
        readinessScore >= 70 -> "Ready"
        readinessScore >= 40 -> "At Risk"
        else                 -> "Danger"
    }
    val readinessColors = when {
        readinessScore >= 70 -> riskColorSet("Safe")
        readinessScore >= 40 -> riskColorSet("Moderate")
        else                 -> riskColorSet("High Risk")
    }
    val riskLabel = when {
        riskScore >= 60 -> "High Risk"
        riskScore >= 40 -> "Moderate"
        else            -> "Safe"
    }

    ElevatedCard(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape     = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(courseColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = course.title.take(1).uppercase(),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    course.title,
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                if (course.instructor.isNotBlank()) {
                    Text(course.instructor,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(6.dp))

                // Metadata row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("$activeCount active",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    course.examDate?.let { ms ->
                        val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ms))
                        Text("Exam $dateStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(Modifier.height(6.dp))

                // Badge row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RiskBadge(riskLabel, riskColors.text)
                    RiskBadge(
                        "$readinessLabel · ${readinessScore}%",
                        readinessColors.text
                    )
                }
            }

            Column {
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit",
                        tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                        tint     = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun RiskBadge(label: String, color: Color) {
    Surface(
        color    = color.copy(alpha = 0.12f),
        shape    = RoundedCornerShape(6.dp)
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color    = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
