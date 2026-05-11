package com.example.studypartner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studypartner.ui.theme.BookmarkGold
import com.example.studypartner.ui.theme.DeepOrange
import com.example.studypartner.ui.theme.Dimens
import com.example.studypartner.ui.theme.LimeCheck
import com.example.studypartner.ui.theme.OrangeCheck
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Courses", fontWeight = FontWeight.SemiBold) },
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
                onClick        = { navController.navigate(Screen.AddCourse.route) },
                expanded       = fabExpanded,
                icon           = { Icon(Icons.Default.Add, contentDescription = null) },
                text           = { Text("New course", style = MaterialTheme.typography.labelLarge) },
                containerColor = DeepOrange,
                contentColor   = Color.White
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->

        if (courses.isEmpty()) {
            EmptyStateView(
                icon        = Icons.Default.School,
                title       = "No courses yet",
                subtitle    = "Add your first course to link tasks and track your readiness per subject.",
                actionLabel = "Add course",
                onAction    = { navController.navigate(Screen.AddCourse.route) },
                modifier    = Modifier
                    .padding(padding)
                    .padding(bottom = outerPadding.calculateBottomPadding())
            )
        } else {
            LazyColumn(
                state          = listState,
                contentPadding = PaddingValues(
                    start  = Dimens.ScreenPadding,
                    end    = Dimens.ScreenPadding,
                    top    = padding.calculateTopPadding() + Dimens.SpaceXs,
                    bottom = outerPadding.calculateBottomPadding() + 96.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
            ) {
                item("hero") {
                    Column(modifier = Modifier.padding(top = Dimens.SpaceSm, bottom = Dimens.SpaceXs)) {
                        Text(
                            "YOUR COURSES",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Dimens.SpaceXs))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text       = "${courses.size}",
                                fontSize   = 72.sp,
                                lineHeight = 76.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color      = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.width(Dimens.SpaceMd))
                            Text(
                                text  = if (courses.size == 1) "course" else "courses",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                    }
                }
                items(courses, key = { it.id }) { course ->
                    val courseTasks = taskList.filter { it.courseId == course.id }
                    PaperCourseCard(
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
private fun PaperCourseCard(
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
    }.getOrDefault(DeepOrange)

    val readinessAccent = when {
        readinessScore >= 70 -> LimeCheck
        readinessScore >= 40 -> OrangeCheck
        else                 -> MaterialTheme.colorScheme.error
    }
    val riskAccent = when {
        riskScore >= 60 -> MaterialTheme.colorScheme.error
        riskScore >= 40 -> OrangeCheck
        else            -> LimeCheck
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color    = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape    = RoundedCornerShape(18.dp),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Color stripe
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(courseColor)
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(Dimens.CardPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(courseColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = course.title.take(1).uppercase(),
                        style      = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                }
                Spacer(Modifier.width(Dimens.SpaceMd))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        course.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (course.instructor.isNotBlank()) {
                        Text(
                            course.instructor,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        StatChip(value = "$activeCount", label = "active", accent = DeepOrange)
                        StatChip(value = "$readinessScore%", label = "ready", accent = readinessAccent)
                        course.examDate?.let { ms ->
                            val dateStr = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ms)).uppercase()
                            Surface(color = BookmarkGold.copy(alpha = 0.18f), shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    "EXAM $dateStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
                Column {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(value: String, label: String, accent: Color) {
    Surface(
        color = accent.copy(alpha = 0.14f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text  = value,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = accent.copy(alpha = 0.75f)
            )
        }
    }
}
