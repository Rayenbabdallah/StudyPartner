package com.example.studypartner

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyTaskDao {

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<StudyTask>>

    @Insert
    suspend fun insertTask(task: StudyTask)
}