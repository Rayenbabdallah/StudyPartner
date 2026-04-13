package com.example.studypartner

import kotlinx.coroutines.flow.Flow

class CourseRepository(private val dao: CourseDao) {
    val allCourses: Flow<List<Course>> = dao.getAllCourses()
    suspend fun insert(course: Course)  = dao.insertCourse(course)
    suspend fun update(course: Course)  = dao.updateCourse(course)
    suspend fun delete(course: Course)  = dao.deleteCourse(course)
}
