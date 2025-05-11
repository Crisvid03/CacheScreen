package com.example.screensense.modulo2.workers

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.CoroutineWorker // Mejor usar CoroutineWorker
import androidx.work.WorkerParameters
import com.example.screensense.modulo2.utils.AppLimit


class AppLimitWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override suspend fun doWork(): Result {
        return try {
            AppLimit.checkAppLimits(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry() // O Result.failure() según tu lógica
        }
    }
}