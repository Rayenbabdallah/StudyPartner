package com.example.studypartner

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first

class NotificationWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val notificationsEnabled = UserPreferences.notificationsEnabled(context).first()
        if (!notificationsEnabled) return Result.success()

        createChannel()

        val db    = AppDatabase.getDatabase(context)
        val tasks = db.taskDao().getAllTasks().first()

        val approaching = tasks.filter { task ->
            !task.isCompleted && task.daysUntilDeadline()?.let { it in 0..1 } == true
        }

        val overdue = tasks.filter { task ->
            !task.isCompleted && task.isOverdue()
        }

        val stuck = tasks.filter { task ->
            !task.isCompleted && task.progress < 20 &&
                task.daysUntilDeadline()?.let { it in 3..7 } == true
        }

        // Panic mode alert
        if (PriorityEngine.isPanicMode(tasks)) {
            val panicCount = PriorityEngine.panicTasks(tasks).size
            sendNotification(
                id      = NOTIF_ID_PANIC,
                title   = "⚠ Survival Mode — $panicCount critical task(s)!",
                message = "You have tasks due very soon with low progress. Open the app now.",
                priority = NotificationCompat.PRIORITY_HIGH
            )
        }

        overdue.forEach { task ->
            sendNotification(
                id      = task.id,
                title   = "Overdue: ${task.title}",
                message = "${task.subject} was due and needs your attention now.",
                priority = NotificationCompat.PRIORITY_HIGH
            )
        }

        approaching.forEach { task ->
            val label = if (task.daysUntilDeadline() == 0) "due today" else "due tomorrow"
            sendNotification(
                id      = task.id + 10_000,
                title   = "Due soon: ${task.title}",
                message = "${task.subject} is $label — ${task.progress}% done. Don't leave it too late."
            )
        }

        stuck.forEach { task ->
            val days = task.daysUntilDeadline() ?: return@forEach
            sendNotification(
                id      = task.id + 20_000,
                title   = "Stuck? ${task.title}",
                message = "${task.progress}% done with $days day(s) left. Even 25 min helps."
            )
        }

        return Result.success()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Deadline Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies you when study task deadlines are approaching"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(
        id: Int,
        title: String,
        message: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (_: SecurityException) {
            // Permission not granted — no-op
        }
    }

    companion object {
        const val CHANNEL_ID    = "deadline_reminders"
        const val NOTIF_ID_PANIC = 99_999
    }
}
