package com.example.screensense.modulo2.bloqueo.services

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.screensense.modulo2.workers.AppLimitWorker
import com.example.screensense.R
import java.util.concurrent.TimeUnit

class AppLimitService : Service() {

    companion object {
        private const val CHANNEL_ID = "app_limit_monitor_channel"
        private const val NOTIFICATION_ID = 1001
        private const val WORKER_TAG = "app_limit_monitor_worker"
        private const val CHECK_INTERVAL_MINUTES = 15L
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        schedulePeriodicCheck()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID).apply {
            setContentTitle(getString(R.string.app_limit_notification_title))
            setContentText(getString(R.string.app_limit_notification_text))
            setSmallIcon(R.drawable.ic_notification)
            priority = NotificationCompat.PRIORITY_LOW
            setOngoing(true)
            setAutoCancel(false)
            setShowWhen(false)
        }.build()
    }

    private fun schedulePeriodicCheck() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<AppLimitWorker>(
            CHECK_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        ).setConstraints(constraints)
            .addTag(WORKER_TAG)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORKER_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_limit_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.app_limit_channel_description)
                setShowBadge(false)
            }

            (getSystemService(NotificationManager::class.java))?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        WorkManager.getInstance(this).cancelAllWorkByTag(WORKER_TAG)
        super.onDestroy()
    }
}