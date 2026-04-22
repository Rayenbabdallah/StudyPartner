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
class StudyTaskDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: StudyTaskDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.taskDao()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun insert_thenQuery_returnsTask() = runTest {
        dao.insertTask(StudyTask(title = "Essay", subject = "Hist", difficulty = 2, urgency = 3))
        val stored = dao.getAllTasks().first()
        assertEquals(1, stored.size)
        assertEquals("Essay", stored[0].title)
    }

    @Test
    fun getAllTasks_orderedByIdDescending() = runTest {
        dao.insertTask(StudyTask(title = "A", subject = "s", difficulty = 1, urgency = 1))
        dao.insertTask(StudyTask(title = "B", subject = "s", difficulty = 1, urgency = 1))
        dao.insertTask(StudyTask(title = "C", subject = "s", difficulty = 1, urgency = 1))
        val titles = dao.getAllTasks().first().map { it.title }
        assertEquals(listOf("C", "B", "A"), titles)
    }

    @Test
    fun updateTask_changesFieldsInPlace() = runTest {
        dao.insertTask(StudyTask(title = "Old", subject = "s", difficulty = 1, urgency = 1))
        val original = dao.getAllTasks().first().first()
        dao.updateTask(original.copy(title = "New", progress = 50))
        val updated = dao.getAllTasks().first().first()
        assertEquals("New", updated.title)
        assertEquals(50, updated.progress)
    }

    @Test
    fun deleteTask_removesRow() = runTest {
        dao.insertTask(StudyTask(title = "Gone", subject = "s", difficulty = 1, urgency = 1))
        val task = dao.getAllTasks().first().first()
        dao.deleteTask(task)
        assertTrue(dao.getAllTasks().first().isEmpty())
    }
}
