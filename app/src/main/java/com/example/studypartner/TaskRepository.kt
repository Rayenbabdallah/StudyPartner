package com.example.studypartner

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: StudyTaskDao) {

    val allTasks: Flow<List<StudyTask>> = dao.getAllTasks()

    suspend fun insert(task: StudyTask) = dao.insertTask(task)

    suspend fun update(task: StudyTask) = dao.updateTask(task)

    suspend fun delete(task: StudyTask) = dao.deleteTask(task)
}
