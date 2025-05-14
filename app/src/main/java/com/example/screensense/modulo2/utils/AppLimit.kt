package com.example.screensense.modulo2.utils

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.screensense.R
import com.example.screensense.modulo2.bloqueo.BlockerActivity

object AppLimit {

    private const val PREFS_NAME = "app_limits"

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun checkAppLimits(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val allEntries = prefs.all

        for ((key, value) in allEntries) {
            if (key.startsWith("limit_")) {
                val packageName = key.removePrefix("limit_")
                val limitMillis = value as? Long ?: continue

                val startTimeKey = "start_time_$packageName"
                val accumulatedKey = "accumulated_time_$packageName"

                val startTime = prefs.getLong(startTimeKey, 0)
                var accumulatedTime = prefs.getLong(accumulatedKey, 0)
                val now = System.currentTimeMillis()

                val isInForeground = isAppInForegroundViaUsageStats(context, packageName)

                Log.d("AppLimit", "📊 Revisando $packageName - StartTime: $startTime Limit: $limitMillis")

                if (isInForeground) {
                    if (startTime != 0L) {
                        val elapsed = now - startTime
                        accumulatedTime += elapsed
                        prefs.edit().putLong(accumulatedKey, accumulatedTime).apply()
                        Log.d("AppLimit", "📱 $packageName en foreground. +$elapsed ms acumulado: $accumulatedTime ms")
                    } else {
                        Log.d("AppLimit", "🆕 $packageName primer startTime")
                    }
                    prefs.edit().putLong(startTimeKey, now).apply()
                } else {
                    Log.d("AppLimit", "📴 $packageName no en foreground. Tiempo acumulado: $accumulatedTime ms")
                    prefs.edit().putLong(startTimeKey, now).apply()
                }

                if (accumulatedTime >= limitMillis) {
                    Log.d("AppLimit", "🚨 Límite superado para $packageName")
                    launchBlocker(context, packageName, accumulatedTime)
                    prefs.edit()
                        .remove("limit_$packageName")
                        .remove(startTimeKey)
                        .remove(accumulatedKey)
                        .apply()
                }
            }
        }
    }

    private fun isAppInForegroundViaUsageStats(context: Context, packageName: String): Boolean {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - 10_000,
            now
        )

        if (stats != null) {
            val recent = stats
                .filter { it.packageName == packageName }
                .maxByOrNull { it.lastTimeUsed }

            if (recent != null) {
                return now - recent.lastTimeUsed <= 10_000 // usado en los últimos 10 seg
            }
        }
        return false
    }

    private fun launchBlocker(context: Context, packageName: String, timeExceeded: Long) {
        Log.d("AppLimit", "🚀 Lanzando BlockerActivity para $packageName")

        val appName = try {
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            ).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown_app)
        }

        val intent = Intent(context, BlockerActivity::class.java).apply {
            putExtra("app_name", appName)
            putExtra("time_exceeded", timeExceeded)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        context.startActivity(intent)
        Log.d("AppLimit", "✅ BlockerActivity lanzada")
    }

    fun setAppLimit(context: Context, packageName: String, hours: Int, minutes: Int) {
        val limitMillis = (hours * 3_600_000L) + (minutes * 60_000L)
        val now = System.currentTimeMillis()

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .putLong("limit_$packageName", limitMillis)
            .putLong("start_time_$packageName", now)
            .putLong("accumulated_time_$packageName", 0)
            .apply()
    }

    fun removeAppLimit(context: Context, packageName: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            .remove("limit_$packageName")
            .remove("start_time_$packageName")
            .remove("accumulated_time_$packageName")
            .apply()
    }

    fun getRemainingTime(context: Context, packageName: String): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val limit = prefs.getLong("limit_$packageName", 0)
        val accumulated = prefs.getLong("accumulated_time_$packageName", 0)

        return if (limit > 0) {
            limit - accumulated
        } else {
            0
        }
    }
}
