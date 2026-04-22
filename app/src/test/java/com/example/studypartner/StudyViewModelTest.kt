package com.example.studypartner

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class StudyViewModelTest {

    @get:Rule
    val instantTaskRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()
    private val scope = TestScope(dispatcher)

    private lateinit var app: Application
    private lateinit var vm: StudyViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        app = ApplicationProvider.getApplicationContext()
        resetAppDatabaseSingleton()
        app.deleteDatabase("studypartner_db")
        vm = StudyViewModel(app)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        resetAppDatabaseSingleton()
        app.deleteDatabase("studypartner_db")
    }

    private fun resetAppDatabaseSingleton() {
        val field = AppDatabase.Companion::class.java.getDeclaredField("INSTANCE")
        field.isAccessible = true
        (field.get(AppDatabase.Companion) as? AppDatabase)?.close()
        field.set(AppDatabase.Companion, null)
    }

    @Test
    fun addTask_withBlankTitle_doesNotInsert() = scope.runTest {
        advanceUntilIdle()
        val initial = (vm.uiState.first { it is TaskUiState.Success } as TaskUiState.Success).tasks.size
        vm.title = "   "
        vm.addTask()
        advanceUntilIdle()
        val after = (vm.uiState.first() as TaskUiState.Success).tasks.size
        assertEquals(initial, after)
    }

    @Test
    fun addTask_insertsWithFormValues_andResetsForm() = scope.runTest {
        advanceUntilIdle()
        val before = (vm.uiState.first { it is TaskUiState.Success } as TaskUiState.Success).tasks.size

        vm.title = "Read Ch.1"
        vm.subject = "Biology"
        vm.difficulty = Level.HIGH
        vm.urgency = Level.CRITICAL
        vm.gradeImpact = GradeImpact.HIGH
        vm.addTask()
        advanceUntilIdle()

        val tasks = (vm.uiState.first() as TaskUiState.Success).tasks
        assertEquals(before + 1, tasks.size)
        val inserted = tasks.first { it.title == "Read Ch.1" }
        assertEquals("Biology", inserted.subject)
        assertEquals(Level.HIGH.value, inserted.difficulty)
        assertEquals(Level.CRITICAL.value, inserted.urgency)
        assertEquals(GradeImpact.HIGH.weight, inserted.gradeWeight)

        // Form was reset
        assertEquals("", vm.title)
        assertEquals("", vm.subject)
        assertNull(vm.selectedCourse)
    }

    @Test
    fun toggleComplete_flipsProgressTo100AndBack() = scope.runTest {
        vm.title = "Essay"
        vm.addTask()
        advanceUntilIdle()
        val task = (vm.uiState.first() as TaskUiState.Success).tasks.first { it.title == "Essay" }

        vm.toggleComplete(task)
        advanceUntilIdle()
        val done = vm.getTaskById(task.id)!!
        assertEquals(100, done.progress)

        vm.toggleComplete(done)
        advanceUntilIdle()
        assertEquals(0, vm.getTaskById(task.id)!!.progress)
    }

    @Test
    fun setProgress_clampsAndPersists() = scope.runTest {
        vm.title = "Essay"
        vm.addTask()
        advanceUntilIdle()
        val task = (vm.uiState.first() as TaskUiState.Success).tasks.first { it.title == "Essay" }

        vm.setProgress(task, 150)
        advanceUntilIdle()
        assertEquals(100, vm.getTaskById(task.id)!!.progress)

        vm.setProgress(task, -20)
        advanceUntilIdle()
        assertEquals(0, vm.getTaskById(task.id)!!.progress)
    }

    @Test
    fun deleteThenUndo_restoresTask() = scope.runTest {
        vm.title = "Temp"
        vm.addTask()
        advanceUntilIdle()
        val task = (vm.uiState.first() as TaskUiState.Success).tasks.first { it.title == "Temp" }

        vm.deleteTask(task)
        advanceUntilIdle()
        assertNull((vm.uiState.first() as TaskUiState.Success).tasks.find { it.title == "Temp" })

        vm.undoDelete()
        advanceUntilIdle()
        assertNotNull((vm.uiState.first() as TaskUiState.Success).tasks.find { it.title == "Temp" })
    }

    @Test
    fun markAsUrgent_setsExtremeUrgency() = scope.runTest {
        vm.title = "Paper"
        vm.urgency = Level.LOW
        vm.addTask()
        advanceUntilIdle()
        val task = (vm.uiState.first() as TaskUiState.Success).tasks.first { it.title == "Paper" }

        vm.markAsUrgent(task)
        advanceUntilIdle()
        assertEquals(Level.EXTREME.value, vm.getTaskById(task.id)!!.urgency)
    }

    @Test
    fun addSubTask_blankIsIgnored() = scope.runTest {
        vm.title = "With subs"
        vm.addTask()
        advanceUntilIdle()
        val task = (vm.uiState.first() as TaskUiState.Success).tasks.first { it.title == "With subs" }

        vm.watchSubtasksForTask(task.id)
        vm.addSubTask(task.id, "")
        advanceUntilIdle()
        assertTrue(vm.currentSubtasks.first().isEmpty())

        vm.addSubTask(task.id, "Read intro")
        advanceUntilIdle()
        assertEquals(listOf("Read intro"), vm.currentSubtasks.first().map { it.title })
    }

    @Test
    fun toggleSubTask_flipsCompletion() = scope.runTest {
        vm.title = "T"
        vm.addTask()
        advanceUntilIdle()
        val task = (vm.uiState.first() as TaskUiState.Success).tasks.first { it.title == "T" }
        vm.watchSubtasksForTask(task.id)
        vm.addSubTask(task.id, "step")
        advanceUntilIdle()
        val sub = vm.currentSubtasks.first().first()

        vm.toggleSubTask(sub)
        advanceUntilIdle()
        assertTrue(vm.currentSubtasks.first().first().isCompleted)
    }

    @Test
    fun deleteTask_alsoDeletesItsSubtasks() = scope.runTest {
        vm.title = "With subs"
        vm.addTask()
        advanceUntilIdle()
        val task = (vm.uiState.first() as TaskUiState.Success).tasks.first { it.title == "With subs" }
        vm.watchSubtasksForTask(task.id)
        vm.addSubTask(task.id, "a")
        vm.addSubTask(task.id, "b")
        advanceUntilIdle()

        vm.deleteTask(task)
        advanceUntilIdle()
        assertTrue(vm.currentSubtasks.first().isEmpty())
    }

    @Test
    fun addCourse_thenGetCourseById_findsIt() = scope.runTest {
        advanceUntilIdle()
        vm.addCourse(Course(title = "Physics"))
        advanceUntilIdle()
        val course = vm.allCourses.first { list -> list.any { it.title == "Physics" } }
            .first { it.title == "Physics" }
        assertEquals(course, vm.getCourseById(course.id))
    }

    @Test
    fun suggestStudySessionPlan_general_fallbackIsBalanced() = scope.runTest {
        advanceUntilIdle()
        val plan = vm.suggestStudySessionPlan(task = null, course = null)
        assertEquals(SessionContextType.GENERAL, plan.contextType)
        assertTrue(plan.durationMinutes in 25..60)
        assertEquals(25, plan.breakEveryMinutes)
        assertEquals(5, plan.breakLengthMinutes)
    }

    @Test
    fun suggestStudySessionPlan_forTask_usesTaskLabelAndContext() = scope.runTest {
        advanceUntilIdle()
        val now = System.currentTimeMillis()
        val task = StudyTask(
            id = 99,
            title = "Critical paper",
            subject = "CS",
            difficulty = Level.EXTREME.value,
            urgency = Level.EXTREME.value,
            deadline = now + 86_400_000L,
            gradeWeight = GradeImpact.CRITICAL.weight,
        )
        val plan = vm.suggestStudySessionPlan(task = task, course = null)
        assertEquals(SessionContextType.TASK, plan.contextType)
        assertEquals(99, plan.taskId)
        assertTrue(plan.contextLabel.contains("Critical paper"))
        assertTrue(plan.durationMinutes in 20..90)
    }

    @Test
    fun saveStudySession_insertsAndExposesViaFlow() = scope.runTest {
        advanceUntilIdle()
        val plan = StudySessionPlan(
            contextType = SessionContextType.GENERAL,
            contextLabel = "Session",
            durationMinutes = 30,
            breakEveryMinutes = 25,
            breakLengthMinutes = 5,
            suggestionReason = "test",
        )
        vm.saveStudySession(plan, name = "My session")
        advanceUntilIdle()
        val sessions = vm.allStudySessions.first { it.any { s -> s.name == "My session" } }
        assertTrue(sessions.any { it.name == "My session" && it.durationMinutes == 30 })
    }

    @Test
    fun getLocalRescuePlan_emptyWhenNoPanic() = scope.runTest {
        advanceUntilIdle()
        assertEquals("No panic tasks detected. Keep working steadily.", vm.getLocalRescuePlan())
    }

    @Test
    fun getLocalRescuePlan_mentionsPanicTaskTitle() = scope.runTest {
        advanceUntilIdle()
        vm.title = "Panic paper"
        vm.urgency = Level.EXTREME
        vm.gradeImpact = GradeImpact.CRITICAL
        vm.deadlineMillis = System.currentTimeMillis() + 86_400_000L
        vm.addTask()
        advanceUntilIdle()
        val plan = vm.getLocalRescuePlan()
        assertTrue(plan.contains("Panic paper"))
        assertTrue(plan.startsWith("RESCUE PLAN"))
    }

    @Test
    fun getLocalAssistantResponse_emptyWhenNoActiveTasks() = scope.runTest {
        advanceUntilIdle()
        // seedIfEmpty adds sample tasks — complete them all
        val tasks = (vm.uiState.first() as TaskUiState.Success).tasks
        tasks.forEach { vm.setProgress(it, 100) }
        advanceUntilIdle()
        val msg = vm.getLocalAssistantResponse("help")
        assertEquals("All tasks are complete — nothing to worry about right now!", msg)
    }

    @Test
    fun getLocalAssistantResponse_planQuestion_listsTopTasks() = scope.runTest {
        advanceUntilIdle()
        vm.title = "X"
        vm.addTask()
        advanceUntilIdle()
        val msg = vm.getLocalAssistantResponse("make me a plan")
        assertTrue(msg.contains("1."))
    }

    @Test
    fun dismissPanic_clearsGhasretMode() = scope.runTest {
        // Create a panic-triggering task
        vm.title = "Panic"
        vm.urgency = Level.EXTREME
        vm.gradeImpact = GradeImpact.CRITICAL
        vm.deadlineMillis = System.currentTimeMillis() + 86_400_000L
        vm.addTask()
        advanceUntilIdle()
        // Panic mode engages
        val panicSeen = vm.appMode.firstOrNull { it == AppMode.GHASRET_LEKLEB } != null
        assertTrue(panicSeen)

        vm.dismissPanic()
        advanceUntilIdle()
        assertEquals(AppMode.NORMAL, vm.appMode.first())
    }

    @Test
    fun clearAssistant_resetsToIdle() = scope.runTest {
        vm.clearAssistant()
        assertEquals(AiState.Idle, vm.assistantState.first())
    }

    @Test
    fun sessionPlanFromStored_parsesUnknownContextTypeAsGeneral() = scope.runTest {
        val session = StudySession(
            id = 1,
            name = "n",
            contextType = "NOT_A_TYPE",
            contextLabel = "x",
            durationMinutes = 25,
            breakEveryMinutes = 25,
            breakLengthMinutes = 5,
            suggestionReason = "r",
        )
        assertEquals(SessionContextType.GENERAL, vm.sessionPlanFromStored(session).contextType)
    }
}
