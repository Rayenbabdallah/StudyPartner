package com.example.studypartner

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tasks ADD COLUMN deadline INTEGER")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE tasks ADD COLUMN isCompleted INTEGER NOT NULL DEFAULT 0")
    }
}

/**
 * Migration 3 → 4:
 * - Recreates `tasks` table: drops `isCompleted`, adds `courseId`, `type`, `gradeWeight`, `progress`
 *   (isCompleted → progress: 0 or 100)
 * - Creates `courses` table
 * - Creates `subtasks` table
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {

        // 1. Courses table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS courses (
                id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title       TEXT    NOT NULL,
                instructor  TEXT    NOT NULL DEFAULT '',
                creditWeight REAL   NOT NULL DEFAULT 0.5,
                colorHex    TEXT    NOT NULL DEFAULT '#3949AB',
                examDate    INTEGER
            )
        """.trimIndent())

        // 2. Recreate tasks with new schema
        database.execSQL("""
            CREATE TABLE tasks_new (
                id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title       TEXT    NOT NULL,
                subject     TEXT    NOT NULL,
                courseId    INTEGER,
                difficulty  INTEGER NOT NULL,
                urgency     INTEGER NOT NULL,
                deadline    INTEGER,
                type        TEXT    NOT NULL DEFAULT 'ASSIGNMENT',
                gradeWeight REAL    NOT NULL DEFAULT 0.5,
                progress    INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())

        database.execSQL("""
            INSERT INTO tasks_new
                (id, title, subject, courseId, difficulty, urgency, deadline, type, gradeWeight, progress)
            SELECT
                id, title, subject, NULL, difficulty, urgency, deadline,
                'ASSIGNMENT', 0.5,
                CASE WHEN isCompleted = 1 THEN 100 ELSE 0 END
            FROM tasks
        """.trimIndent())

        database.execSQL("DROP TABLE tasks")
        database.execSQL("ALTER TABLE tasks_new RENAME TO tasks")

        // 3. Subtasks table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS subtasks (
                id          INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                taskId      INTEGER NOT NULL,
                title       TEXT    NOT NULL,
                isCompleted INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS study_sessions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                contextType TEXT NOT NULL,
                contextLabel TEXT NOT NULL,
                taskId INTEGER,
                courseId INTEGER,
                durationMinutes INTEGER NOT NULL,
                breakEveryMinutes INTEGER NOT NULL,
                breakLengthMinutes INTEGER NOT NULL,
                suggestionReason TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

@Database(
    entities  = [StudyTask::class, Course::class, SubTask::class, StudySession::class],
    version   = 5
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): StudyTaskDao
    abstract fun courseDao(): CourseDao
    abstract fun subTaskDao(): SubTaskDao
    abstract fun studySessionDao(): StudySessionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studypartner_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
