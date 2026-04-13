package com.example.studypartner

import kotlinx.coroutines.flow.Flow

class StudySessionRepository(private val dao: StudySessionDao) {
    val allStudySessions: Flow<List<StudySession>> = dao.getAllStudySessions()

    suspend fun insert(session: StudySession): Long = dao.insertStudySession(session)

    suspend fun update(session: StudySession) = dao.updateStudySession(session)

    suspend fun delete(session: StudySession) = dao.deleteStudySession(session)
}

