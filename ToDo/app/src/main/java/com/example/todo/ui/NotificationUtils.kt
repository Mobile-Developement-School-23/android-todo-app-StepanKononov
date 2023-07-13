package com.example.todo.ui


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.todo.Constants
import com.example.todo.MainActivity
import com.example.todo.R
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationUtils @Inject constructor(var workManager: WorkManager) {

    fun createNotification(context: Context, taskText: String, deadlineMillis: Date, id: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager, id)


        val deadlineTimeMillis = deadlineMillis.time

        if (deadlineTimeMillis > System.currentTimeMillis()) {
            // Создание входных данных для Worker
            val inputData = Data.Builder()
                .putString(NotificationWorker.KEY_TASK_TEXT, taskText)
                .putString(NotificationWorker.KEY_NOTIFICATION_ID, id)
                .build()

            // Создание запроса на выполнение работы с уведомлением
            val notificationRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(deadlineTimeMillis - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .build()

            // Запуск работы по расписанию с уникальным идентификатором (id)
            workManager.enqueueUniqueWork(id, ExistingWorkPolicy.REPLACE, notificationRequest)
        }
    }

    fun cancelNotification(id: String) {
        workManager.cancelUniqueWork(id)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager, id: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return
        val channel = NotificationChannel(
            id,
            Constants.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = Constants.CHANNEL_DESCRIPTION
        }
        notificationManager.createNotificationChannel(channel)
    }

}


class NotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // Получите данные из входных параметров
        val taskText = inputData.getString(KEY_TASK_TEXT)
        val notificationId = inputData.getString(KEY_NOTIFICATION_ID)

        if (!taskText.isNullOrEmpty() && !notificationId.isNullOrEmpty()) {
            showNotification(taskText, notificationId)
            return Result.success()
        }

        return Result.failure()
    }

    private fun showNotification(taskText: String, notificationId: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager, notificationId)
        val pendingIntent = createPendingIntent(context)
        val notification = buildNotification(context, notificationId, taskText, pendingIntent)
        notificationManager.notify(notificationId.hashCode(), notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager, id: String) {
        // Проверка версии SDK для создания канала уведомлений
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return

        val channel = NotificationChannel(
            id,
            Constants.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = Constants.CHANNEL_DESCRIPTION
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun createPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun buildNotification(
        context: Context,
        id: String,
        taskText: String,
        pendingIntent: PendingIntent,
    ): Notification {
        return NotificationCompat.Builder(context, id)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(taskText)
            .setSmallIcon(R.drawable.ic_priority_high)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    companion object {
        const val KEY_TASK_TEXT = "task_text"
        const val KEY_NOTIFICATION_ID = "notification_id"
    }
}
