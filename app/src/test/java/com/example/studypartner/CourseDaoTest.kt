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
class CourseDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CourseDao

    @Before
    fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.courseDao()
    }

    @After
    fun tearDown() { db.close() }

    @Test
    fun insert_thenQuery_sortedAlphabetically() = runTest {
        dao.insertCourse(Course(title = "Networks"))
        dao.insertCourse(Course(title = "Algorithms"))
        dao.insertCourse(Course(title = "Databases"))
        val titles = dao.getAllCourses().first().map { it.title }
        assertEquals(listOf("Algorithms", "Databases", "Networks"), titles)
    }

    @Test
    fun insert_withSameId_replacesExistingRow() = runTest {
        dao.insertCourse(Course(id = 1, title = "Old"))
        dao.insertCourse(Course(id = 1, title = "New"))
        val rows = dao.getAllCourses().first()
        assertEquals(1, rows.size)
        assertEquals("New", rows[0].title)
    }

    @Test
    fun update_persistsExamDate() = runTest {
        dao.insertCourse(Course(title = "Calc"))
        val original = dao.getAllCourses().first().first()
        dao.updateCourse(original.copy(examDate = 1234567L))
        assertEquals(1234567L, dao.getAllCourses().first().first().examDate)
    }

    @Test
    fun delete_removesCourse() = runTest {
        dao.insertCourse(Course(title = "Gone"))
        dao.deleteCourse(dao.getAllCourses().first().first())
        assertTrue(dao.getAllCourses().first().isEmpty())
    }
}
