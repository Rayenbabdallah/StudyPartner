package com.example.studypartner

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var tasks: TaskRepository
    private lateinit var courses: CourseRepository
    private lateinit var sessions: StudySessionRepository

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tasks    = TaskRepository(db.taskDao())
        courses  = CourseRepository(db.courseDao())
        sessions = StudySessionRepository(db.studySessionDao())
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun taskRepository_exposesInsertedTasks() = runTest {
        tasks.insert(StudyTask(title = "A", subject = "s", difficulty = 1, urgency = 1))
        assertEquals(1, tasks.allTasks.first().size)
    }

    @Test
    fun taskRepository_updateAndDeletePropagate() = runTest {
        tasks.insert(StudyTask(title = "A", subject = "s", difficulty = 1, urgency = 1))
        val t = tasks.allTasks.first().first()
        tasks.update(t.copy(progress = 100))
        assertEquals(100, tasks.allTasks.first().first().progress)
        tasks.delete(tasks.allTasks.first().first())
        assertTrue(tasks.allTasks.first().isEmpty())
    }

    @Test
    fun courseRepository_exposesInsertedCourses() = runTest {
        courses.insert(Course(title = "X"))
        assertEquals(1, courses.allCourses.first().size)
    }

    @Test
    fun studySessionRepository_insertReturnsId() = runTest {
        val id = sessions.insert(
            StudySession(
                name = "S",
                contextType = SessionContextType.GENERAL.name,
                contextLabel = "General",
                durationMinutes = 30,
                breakEveryMinutes = 25,
                breakLengthMinutes = 5,
                suggestionReason = "test",
            )
        )
        assertEquals(id, sessions.allStudySessions.first().first().id.toLong())
    }
}
