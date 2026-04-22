package com.example.studypartner

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StudySessionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: StudySessionDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.studySessionDao()
    }

    @After
    fun tearDown() { db.close() }

    private fun newSession(
        name: String,
        updatedAt: Long = System.currentTimeMillis(),
    ) = StudySession(
        name = name,
        contextType = SessionContextType.GENERAL.name,
        contextLabel = "General",
        durationMinutes = 25,
        breakEveryMinutes = 25,
        breakLengthMinutes = 5,
        suggestionReason = "test",
        updatedAt = updatedAt,
    )

    @Test
    fun insert_returnsGeneratedId() = runTest {
        val id = dao.insertStudySession(newSession("A"))
        assertNotEquals(0L, id)
    }

    @Test
    fun getAll_orderedByUpdatedAtDescending() = runTest {
        dao.insertStudySession(newSession("old", updatedAt = 1000L))
        dao.insertStudySession(newSession("new", updatedAt = 3000L))
        dao.insertStudySession(newSession("mid", updatedAt = 2000L))
        val names = dao.getAllStudySessions().first().map { it.name }
        assertEquals(listOf("new", "mid", "old"), names)
    }

    @Test
    fun update_persistsNewDuration() = runTest {
        val id = dao.insertStudySession(newSession("A")).toInt()
        val stored = dao.getAllStudySessions().first().first { it.id == id }
        dao.updateStudySession(stored.copy(durationMinutes = 75))
        assertEquals(75, dao.getAllStudySessions().first().first { it.id == id }.durationMinutes)
    }

    @Test
    fun delete_removesRow() = runTest {
        dao.insertStudySession(newSession("A"))
        dao.deleteStudySession(dao.getAllStudySessions().first().first())
        assertTrue(dao.getAllStudySessions().first().isEmpty())
    }
}
