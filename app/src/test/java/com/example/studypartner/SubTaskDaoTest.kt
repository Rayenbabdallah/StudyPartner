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
class SubTaskDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SubTaskDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.subTaskDao()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun getSubtasksForTask_returnsOnlyMatchingParent() = runTest {
        dao.insertSubTask(SubTask(taskId = 1, title = "a"))
        dao.insertSubTask(SubTask(taskId = 1, title = "b"))
        dao.insertSubTask(SubTask(taskId = 2, title = "c"))
        val forOne = dao.getSubtasksForTask(1).first()
        assertEquals(2, forOne.size)
        assertEquals(listOf("a", "b"), forOne.map { it.title })
    }

    @Test
    fun updateSubTask_togglesCompletedFlag() = runTest {
        dao.insertSubTask(SubTask(taskId = 1, title = "a"))
        val original = dao.getSubtasksForTask(1).first().first()
        dao.updateSubTask(original.copy(isCompleted = true))
        assertTrue(dao.getSubtasksForTask(1).first().first().isCompleted)
    }

    @Test
    fun deleteAllForTask_removesOnlyThatParent() = runTest {
        dao.insertSubTask(SubTask(taskId = 1, title = "a"))
        dao.insertSubTask(SubTask(taskId = 1, title = "b"))
        dao.insertSubTask(SubTask(taskId = 2, title = "c"))
        dao.deleteAllForTask(1)
        assertTrue(dao.getSubtasksForTask(1).first().isEmpty())
        assertEquals(1, dao.getSubtasksForTask(2).first().size)
    }
}
