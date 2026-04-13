package com.example.studypartner

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SessionContextType { GENERAL, TASK, COURSE }

data class StudySessionPlan(
    val contextType: SessionContextType = SessionContextType.GENERAL,
    val contextLabel: String = "General Study",
    val taskId: Int? = null,
    val courseId: Int? = null,
    val durationMinutes: Int = 25,
    val breakEveryMinutes: Int = 25,
    val breakLengthMinutes: Int = 5,
    val suggestionReason: String = "Balanced focus session."
)

class StudyViewModel(application: Application) : AndroidViewModel(application) {

    private val db               = AppDatabase.getDatabase(application)
    private val repository       = TaskRepository(db.taskDao())
    private val courseRepository = CourseRepository(db.courseDao())
    private val studySessionRepository = StudySessionRepository(db.studySessionDao())
    private val subTaskDao       = db.subTaskDao()
    private var defaultDifficultySetting = Level.LOW
    private var defaultUrgencySetting = Level.LOW

    // ── Task form state ───────────────────────────────────────────────────────
    var title          by mutableStateOf("")
    var subject        by mutableStateOf("")
    var selectedCourse by mutableStateOf<Course?>(null)
    var difficulty     by mutableStateOf(Level.LOW)
    var urgency        by mutableStateOf(Level.LOW)
    var deadlineMillis by mutableStateOf<Long?>(null)
    var taskType       by mutableStateOf(TaskType.ASSIGNMENT)
    var gradeImpact    by mutableStateOf(GradeImpact.MEDIUM)
    var taskProgress   by mutableIntStateOf(0)

    // ── Task list ─────────────────────────────────────────────────────────────
    val uiState: StateFlow<TaskUiState> = repository.allTasks
        .map<List<StudyTask>, TaskUiState> { tasks -> TaskUiState.Success(tasks) }
        .catch { emit(TaskUiState.Error(it.message ?: "Failed to load tasks")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskUiState.Loading)

    private val tasks: List<StudyTask>
        get() = (uiState.value as? TaskUiState.Success)?.tasks ?: emptyList()

    // ── Courses ───────────────────────────────────────────────────────────────
    val allCourses: StateFlow<List<Course>> = courseRepository.allCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Session context and AI-style suggestion state
    private val _activeSessionPlan = MutableStateFlow(StudySessionPlan())
    val activeSessionPlan: StateFlow<StudySessionPlan> = _activeSessionPlan.asStateFlow()
    val allStudySessions: StateFlow<List<StudySession>> = studySessionRepository.allStudySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Subtasks (for currently viewed task) ──────────────────────────────────
    private val _currentTaskId = MutableStateFlow<Int?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentSubtasks: StateFlow<List<SubTask>> = _currentTaskId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else subTaskDao.getSubtasksForTask(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun watchSubtasksForTask(taskId: Int) { _currentTaskId.value = taskId }

    // ── AppMode / Panic ───────────────────────────────────────────────────────
    private val _panicDismissed = MutableStateFlow(false)

    val appMode: StateFlow<AppMode> = combine(uiState, _panicDismissed) { state, dismissed ->
        val t = (state as? TaskUiState.Success)?.tasks ?: emptyList()
        if (!dismissed && PriorityEngine.isPanicMode(t)) AppMode.GHASRET_LEKLEB
        else AppMode.NORMAL
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppMode.NORMAL)

    fun dismissPanic() { _panicDismissed.value = true }

    // Increment panic count when mode first activates
    init {
        viewModelScope.launch {
            defaultDifficultySetting = Level.fromValue(UserPreferences.defaultDifficulty(application).first())
            defaultUrgencySetting = Level.fromValue(UserPreferences.defaultUrgency(application).first())
            difficulty = defaultDifficultySetting
            urgency = defaultUrgencySetting
        }
        viewModelScope.launch {
            var previous = AppMode.NORMAL
            appMode.collect { mode ->
                if (mode == AppMode.GHASRET_LEKLEB && previous == AppMode.NORMAL) {
                    UserPreferences.incrementPanicCount(application)
                }
                previous = mode
            }
        }
        viewModelScope.launch { seedIfEmpty() }
    }

    // ── Seed ──────────────────────────────────────────────────────────────────
    private suspend fun seedIfEmpty() {
        val existingTasks   = repository.allTasks.first()
        val existingCourses = courseRepository.allCourses.first()
        if (existingTasks.isNotEmpty() || existingCourses.isNotEmpty()) return

        val now = System.currentTimeMillis()
        fun days(d: Int): Long = now + d * 86_400_000L

        // ── Courses ───────────────────────────────────────────────────────────
        val seedCourses = listOf(
            Course(id = 1, title = "Algorithms & Data Structures",
                instructor = "Dr. Chen",   creditWeight = 1.0f,  colorHex = "#3949AB", examDate = days(20)),
            Course(id = 2, title = "Computer Networks",
                instructor = "Prof. Martinez", creditWeight = 0.75f, colorHex = "#00897B", examDate = days(25)),
            Course(id = 3, title = "Database Systems",
                instructor = "Dr. Smith",  creditWeight = 0.75f, colorHex = "#F57C00", examDate = days(15)),
            Course(id = 4, title = "Linear Algebra",
                instructor = "Prof. Johnson", creditWeight = 0.5f,  colorHex = "#8E24AA", examDate = days(30)),
            Course(id = 5, title = "Software Engineering",
                instructor = "Dr. Patel",  creditWeight = 1.0f,  colorHex = "#039BE5", examDate = null)
        )
        seedCourses.forEach { courseRepository.insert(it) }

        // ── Tasks ─────────────────────────────────────────────────────────────
        val seedTasks = listOf(
            // HIGH RISK — panic triggers
            StudyTask(title = "Exam Review — Sorting Algorithms",
                subject = "Algorithms & Data Structures", courseId = 1,
                difficulty = Level.EXTREME.value, urgency = Level.EXTREME.value,
                deadline = days(3),  type = TaskType.EXAM.name,
                gradeWeight = GradeImpact.CRITICAL.weight, progress = 20),
            StudyTask(title = "DB Query Optimization Quiz",
                subject = "Database Systems", courseId = 3,
                difficulty = Level.MEDIUM.value, urgency = Level.EXTREME.value,
                deadline = days(1),  type = TaskType.QUIZ.name,
                gradeWeight = GradeImpact.HIGH.weight,     progress = 0),
            StudyTask(title = "SE Group Project Demo",
                subject = "Software Engineering", courseId = 5,
                difficulty = Level.EXTREME.value, urgency = Level.EXTREME.value,
                deadline = days(6),  type = TaskType.PROJECT.name,
                gradeWeight = GradeImpact.CRITICAL.weight, progress = 50),
            // MODERATE
            StudyTask(title = "ER Diagram Assignment",
                subject = "Database Systems", courseId = 3,
                difficulty = Level.HIGH.value, urgency = Level.CRITICAL.value,
                deadline = days(4),  type = TaskType.ASSIGNMENT.name,
                gradeWeight = GradeImpact.HIGH.weight,     progress = 40),
            StudyTask(title = "Network Protocol Lab Report",
                subject = "Computer Networks", courseId = 2,
                difficulty = Level.MEDIUM.value, urgency = Level.HIGH.value,
                deadline = days(5),  type = TaskType.ASSIGNMENT.name,
                gradeWeight = GradeImpact.MEDIUM.weight,   progress = 0),
            StudyTask(title = "Algorithm Problem Set #3",
                subject = "Algorithms & Data Structures", courseId = 1,
                difficulty = Level.CRITICAL.value, urgency = Level.MEDIUM.value,
                deadline = days(8),  type = TaskType.ASSIGNMENT.name,
                gradeWeight = GradeImpact.HIGH.weight,     progress = 60),
            StudyTask(title = "SE UML Diagrams",
                subject = "Software Engineering", courseId = 5,
                difficulty = Level.HIGH.value, urgency = Level.HIGH.value,
                deadline = days(9),  type = TaskType.ASSIGNMENT.name,
                gradeWeight = GradeImpact.HIGH.weight,     progress = 0),
            // SAFE
            StudyTask(title = "Linear Algebra HW #5",
                subject = "Linear Algebra", courseId = 4,
                difficulty = Level.HIGH.value, urgency = Level.LOW.value,
                deadline = days(12), type = TaskType.ASSIGNMENT.name,
                gradeWeight = GradeImpact.MEDIUM.weight,   progress = 30),
            StudyTask(title = "Read Ch.8 — TCP/IP Stack",
                subject = "Computer Networks", courseId = 2,
                difficulty = Level.LOW.value, urgency = Level.MEDIUM.value,
                deadline = days(4),  type = TaskType.READING.name,
                gradeWeight = GradeImpact.LOW.weight,      progress = 60),
            // COMPLETED
            StudyTask(title = "SQL Joins Practice",
                subject = "Database Systems", courseId = 3,
                difficulty = Level.MEDIUM.value, urgency = Level.LOW.value,
                deadline = days(-2), type = TaskType.REVIEW.name,
                gradeWeight = GradeImpact.MINIMAL.weight,  progress = 100)
        )
        seedTasks.forEach { repository.insert(it) }
    }

    // ── CRUD — Tasks ──────────────────────────────────────────────────────────
    fun prepareNewTaskForm() {
        resetTaskForm()
    }

    fun addTask() {
        if (title.isBlank()) return
        val newTask = StudyTask(
            title       = title,
            subject     = selectedCourse?.title ?: subject,
            courseId    = selectedCourse?.id,
            difficulty  = difficulty.value,
            urgency     = urgency.value,
            deadline    = deadlineMillis,
            type        = taskType.name,
            gradeWeight = gradeImpact.weight,
            progress    = 0
        )
        viewModelScope.launch { repository.insert(newTask) }
        resetTaskForm()
    }

    fun updateTask(
        original: StudyTask,
        title: String,
        subject: String,
        courseId: Int?,
        difficulty: Level,
        urgency: Level,
        deadlineMillis: Long?,
        type: TaskType,
        gradeImpact: GradeImpact,
        progress: Int
    ) {
        viewModelScope.launch {
            repository.update(
                original.copy(
                    title       = title,
                    subject     = subject,
                    courseId    = courseId,
                    difficulty  = difficulty.value,
                    urgency     = urgency.value,
                    deadline    = deadlineMillis,
                    type        = type.name,
                    gradeWeight = gradeImpact.weight,
                    progress    = progress
                )
            )
        }
    }

    fun markAsUrgent(task: StudyTask)  { viewModelScope.launch { repository.update(task.markAsUrgent()) } }
    fun toggleComplete(task: StudyTask){ viewModelScope.launch { repository.update(task.toggleCompleted()) } }
    fun setProgress(task: StudyTask, pct: Int) { viewModelScope.launch { repository.update(task.withProgress(pct)) } }

    private var lastDeleted: StudyTask? = null

    fun deleteTask(task: StudyTask) {
        lastDeleted = task
        viewModelScope.launch {
            subTaskDao.deleteAllForTask(task.id)
            repository.delete(task)
        }
    }

    fun undoDelete() {
        lastDeleted?.let { task ->
            viewModelScope.launch { repository.insert(task) }
            lastDeleted = null
        }
    }

    fun getTaskById(id: Int): StudyTask? = tasks.find { it.id == id }
    fun getCourseById(id: Int): Course? = allCourses.value.find { it.id == id }

    private fun resetTaskForm() {
        title = ""; subject = ""; selectedCourse = null
        difficulty = defaultDifficultySetting; urgency = defaultUrgencySetting
        deadlineMillis = null; taskType = TaskType.ASSIGNMENT
        gradeImpact = GradeImpact.MEDIUM; taskProgress = 0
    }

    // ── CRUD — Courses ────────────────────────────────────────────────────────
    fun addCourse(course: Course)    = viewModelScope.launch { courseRepository.insert(course) }
    fun updateCourse(course: Course) = viewModelScope.launch { courseRepository.update(course) }
    fun deleteCourse(course: Course) = viewModelScope.launch { courseRepository.delete(course) }

    // ── CRUD — Subtasks ───────────────────────────────────────────────────────
    fun addSubTask(taskId: Int, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { subTaskDao.insertSubTask(SubTask(taskId = taskId, title = title)) }
    }
    fun toggleSubTask(sub: SubTask)  { viewModelScope.launch { subTaskDao.updateSubTask(sub.copy(isCompleted = !sub.isCompleted)) } }
    fun deleteSubTask(sub: SubTask)  { viewModelScope.launch { subTaskDao.deleteSubTask(sub) } }

    // ── Analytics ─────────────────────────────────────────────────────────────
    fun getAdvice(): String                     = PriorityEngine.getAdvice(tasks)
    fun sortedTasks(): List<StudyTask>          = PriorityEngine.sortByPriority(tasks)
    fun studyLoad(): Int                        = PriorityEngine.studyLoad(tasks)
    fun recommendedSession(): Int               = PriorityEngine.recommendedSessionMinutes(tasks)
    fun subjectBreakdown(): Map<String, Double> = PriorityEngine.subjectBreakdown(tasks)
    fun courseRiskScore(course: Course): Double = PriorityEngine.courseRiskScore(tasks, course)
    fun studyReadinessScore(course: Course): Int= PriorityEngine.studyReadinessScore(tasks, course)
    fun scoreBreakdown(task: StudyTask)         = PriorityEngine.scoreBreakdown(task)

    fun suggestStudySessionPlan(task: StudyTask?, course: Course?): StudySessionPlan {
        val base = recommendedSession().coerceIn(20, 90)

        if (task != null) {
            val score = task.score()
            val duration = when {
                score >= 85 -> 65
                score >= 70 -> 55
                score >= 55 -> 45
                else -> base.coerceAtLeast(30)
            } + if (task.isOverdue()) 10 else 0

            val breakEvery = if (duration >= 55) 25 else 20
            val breakLength = if (duration >= 60) 10 else 5
            val courseLabel = task.courseId?.let { id -> getCourseById(id)?.title } ?: task.subject

            return StudySessionPlan(
                contextType = SessionContextType.TASK,
                contextLabel = "Task: ${task.title}",
                taskId = task.id,
                courseId = task.courseId,
                durationMinutes = duration.coerceIn(20, 90),
                breakEveryMinutes = breakEvery,
                breakLengthMinutes = breakLength,
                suggestionReason = "High focus needed for ${task.priorityLabel().lowercase()} priority in $courseLabel."
            )
        }

        if (course != null) {
            val risk = courseRiskScore(course)
            val duration = when {
                risk >= 70 -> 60
                risk >= 50 -> 50
                risk >= 35 -> 40
                else -> base.coerceAtLeast(30)
            }
            val breakEvery = if (duration >= 50) 25 else 20
            val breakLength = if (duration >= 55) 10 else 5

            return StudySessionPlan(
                contextType = SessionContextType.COURSE,
                contextLabel = "Course: ${course.title}",
                taskId = null,
                courseId = course.id,
                durationMinutes = duration.coerceIn(20, 90),
                breakEveryMinutes = breakEvery,
                breakLengthMinutes = breakLength,
                suggestionReason = "Session adjusted to course risk score ${String.format("%.0f", risk)}/100."
            )
        }

        val fallback = base.coerceIn(25, 60)
        return StudySessionPlan(
            contextType = SessionContextType.GENERAL,
            contextLabel = "General Study",
            taskId = null,
            courseId = null,
            durationMinutes = fallback,
            breakEveryMinutes = 25,
            breakLengthMinutes = 5,
            suggestionReason = "Balanced session from current workload."
        )
    }

    fun setActiveSessionPlan(plan: StudySessionPlan) {
        _activeSessionPlan.value = plan
    }

    private fun parseSessionContextType(raw: String): SessionContextType =
        runCatching { SessionContextType.valueOf(raw) }.getOrDefault(SessionContextType.GENERAL)

    fun sessionPlanFromStored(session: StudySession): StudySessionPlan = StudySessionPlan(
        contextType = parseSessionContextType(session.contextType),
        contextLabel = session.contextLabel,
        taskId = session.taskId,
        courseId = session.courseId,
        durationMinutes = session.durationMinutes,
        breakEveryMinutes = session.breakEveryMinutes,
        breakLengthMinutes = session.breakLengthMinutes,
        suggestionReason = session.suggestionReason
    )

    fun getSessionById(id: Int): StudySession? = allStudySessions.value.find { it.id == id }

    fun activateSessionById(sessionId: Int) {
        getSessionById(sessionId)?.let { session ->
            _activeSessionPlan.value = sessionPlanFromStored(session)
        }
    }

    fun saveStudySession(
        plan: StudySessionPlan,
        name: String,
        existingSessionId: Int? = null,
        onSaved: (Int) -> Unit = {}
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            if (existingSessionId == null) {
                val id = studySessionRepository.insert(
                    StudySession(
                        name = name.ifBlank { plan.contextLabel },
                        contextType = plan.contextType.name,
                        contextLabel = plan.contextLabel,
                        taskId = plan.taskId,
                        courseId = plan.courseId,
                        durationMinutes = plan.durationMinutes,
                        breakEveryMinutes = plan.breakEveryMinutes,
                        breakLengthMinutes = plan.breakLengthMinutes,
                        suggestionReason = plan.suggestionReason,
                        createdAt = now,
                        updatedAt = now
                    )
                ).toInt()
                onSaved(id)
            } else {
                val current = getSessionById(existingSessionId)
                val updated = StudySession(
                    id = existingSessionId,
                    name = name.ifBlank { plan.contextLabel },
                    contextType = plan.contextType.name,
                    contextLabel = plan.contextLabel,
                    taskId = plan.taskId,
                    courseId = plan.courseId,
                    durationMinutes = plan.durationMinutes,
                    breakEveryMinutes = plan.breakEveryMinutes,
                    breakLengthMinutes = plan.breakLengthMinutes,
                    suggestionReason = plan.suggestionReason,
                    createdAt = current?.createdAt ?: now,
                    updatedAt = now
                )
                studySessionRepository.update(updated)
                onSaved(existingSessionId)
            }
        }
    }

    fun deleteStudySession(session: StudySession) {
        viewModelScope.launch { studySessionRepository.delete(session) }
    }

    fun startSessionForTask(taskId: Int): StudySessionPlan {
        val task = getTaskById(taskId)
        val course = task?.courseId?.let { getCourseById(it) }
        val plan = suggestStudySessionPlan(task, course)
        _activeSessionPlan.value = plan
        return plan
    }

    fun startSessionForCourse(courseId: Int): StudySessionPlan {
        val course = getCourseById(courseId)
        val plan = suggestStudySessionPlan(null, course)
        _activeSessionPlan.value = plan
        return plan
    }

    // ── Survival / panic helpers ──────────────────────────────────────────────
    fun panicTasks(): List<StudyTask>      = PriorityEngine.panicTasks(tasks)
    fun survivalScore(task: StudyTask)     = PriorityEngine.survivalScore(task)
    fun survivalLabel(task: StudyTask, strings: StringSet = AppStrings.normal) =
        PriorityEngine.survivalLabel(task, strings)
    fun minimumViableWork(task: StudyTask) = PriorityEngine.minimumViableWork(task)
    fun riskOnsetDays(task: StudyTask)     = PriorityEngine.riskOnsetDays(task)

    fun getLocalRescuePlan(): String {
        val panic = panicTasks()
        if (panic.isEmpty()) return "No panic tasks detected. Keep working steadily."
        val top = panic.first()
        return buildString {
            appendLine("RESCUE PLAN — ${panic.size} task(s) need immediate action:")
            appendLine()
            panic.forEachIndexed { i, t ->
                val d = t.daysUntilDeadline()
                val ds = when { d == null -> "no deadline"; d < 0 -> "OVERDUE"; d == 0 -> "due TODAY"; else -> "due in ${d}d" }
                appendLine("${i + 1}. [${PriorityEngine.survivalLabel(t)}] \"${t.title}\" — $ds, ${t.progress}% done")
                appendLine("   → ${PriorityEngine.minimumViableWork(t)}")
            }
            appendLine()
            append("START NOW: Focus on \"${top.title}\" for the next 25–90 minutes.")
        }.trim()
    }

    // ── AI — home card ────────────────────────────────────────────────────────
    private val _ollamaState = MutableStateFlow<AiState>(AiState.Idle)
    val ollamaState: StateFlow<AiState> = _ollamaState.asStateFlow()

    fun fetchOllamaAdvice() {
        viewModelScope.launch {
            _ollamaState.value = AiState.Loading
            _ollamaState.value = AiRepository.getAdvice(tasks, getApplication()).fold(
                onSuccess = { AiState.Success(it) },
                onFailure = { AiState.Failure(it.message ?: "Unknown error") }
            )
        }
    }

    // ── AI — AI assistant ─────────────────────────────────────────────────────
    private val _assistantState = MutableStateFlow<AiState>(AiState.Idle)
    val assistantState: StateFlow<AiState> = _assistantState.asStateFlow()

    fun fetchAssistantResponse(question: String) {
        viewModelScope.launch {
            _assistantState.value = AiState.Loading
            _assistantState.value = AiRepository.getAssistantResponse(tasks, question, getApplication()).fold(
                onSuccess = { AiState.Success(it) },
                onFailure = { AiState.Failure(it.message ?: "Unknown error") }
            )
        }
    }

    fun clearAssistant() { _assistantState.value = AiState.Idle }

    fun getLocalAssistantResponse(question: String): String {
        val active = tasks.activeTasks()
        return when {
            active.isEmpty() -> "All tasks are complete — nothing to worry about right now!"
            question.contains("overwhelm", ignoreCase = true) ->
                "Take a breath. Focus on ONE task: \"${PriorityEngine.sortByPriority(active).first().title}\". " +
                "Set a 25-minute timer and ignore everything else."
            question.contains("plan", ignoreCase = true) ->
                PriorityEngine.sortByPriority(active).take(3)
                    .mapIndexed { i, t -> "${i + 1}. \"${t.title}\" — score ${String.format("%.0f", t.score())}/100" }
                    .joinToString("\n")
            question.contains("risk", ignoreCase = true) -> {
                val top = PriorityEngine.sortByPriority(active).firstOrNull()
                if (top != null)
                    "\"${top.title}\" scores ${String.format("%.0f", top.score())}/100. " +
                    "It is ${top.gradeImpact.label} grade impact, ${top.riskLabel()} risk, ${top.progress}% complete."
                else "No risky tasks detected."
            }
            else -> getAdvice()
        }
    }

    // ── AI — rescue plan ──────────────────────────────────────────────────────
    private val _rescuePlanState = MutableStateFlow<AiState>(AiState.Idle)
    val rescuePlanState: StateFlow<AiState> = _rescuePlanState.asStateFlow()

    fun fetchRescuePlan() {
        val panic = panicTasks()
        viewModelScope.launch {
            _rescuePlanState.value = AiState.Loading
            _rescuePlanState.value = AiRepository.getRescuePlan(panic, tasks, getApplication()).fold(
                onSuccess = { AiState.Success(it) },
                onFailure = { AiState.Failure(it.message ?: "Unknown error") }
            )
        }
    }

    fun clearRescuePlan() { _rescuePlanState.value = AiState.Idle }

    // ── Settings ─────────────────────────────────────────────────────────────
    fun saveOpenRouterApiKey(key: String) {
        viewModelScope.launch { UserPreferences.setOpenRouterApiKey(getApplication(), key) }
    }
}
