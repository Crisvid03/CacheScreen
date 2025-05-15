package com.example.screensense.modulo2.utils

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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

                val isInForeground = isAppInForeground(context, packageName)

                Log.d("AppLimit", "📊 Revisando $packageName - StartTime: $startTime Limit: $limitMillis")

                if (isInForeground) {
                    if (startTime == 0L) {
                        // Primer inicio en foreground
                        prefs.edit().putLong(startTimeKey, now).apply()
                        Log.d("AppLimit", "🆕 $packageName primer startTime")
                    } else {
                        // App sigue en foreground, acumulamos tiempo
                        val elapsed = now - startTime
                        val newAccumulated = accumulatedTime + elapsed
                        prefs.edit()
                            .putLong(accumulatedKey, newAccumulated)
                            .putLong(startTimeKey, now) // Reiniciamos el contador
                            .apply()
                        Log.d("AppLimit", "📱 $packageName en foreground. +$elapsed ms acumulado: $newAccumulated ms")

                        // Verificamos límite después de actualizar
                        if (newAccumulated >= limitMillis) {
                            handleLimitExceeded(context, prefs, packageName, newAccumulated)
                        }
                    }
                } else {
                    // App no está en foreground, reiniciamos startTime pero mantenemos accumulated
                    if (startTime != 0L) {
                        prefs.edit().putLong(startTimeKey, 0).apply()
                        Log.d("AppLimit", "📴 $packageName salió de foreground. Tiempo acumulado: $accumulatedTime ms")
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun isAppInForeground(context: Context, packageName: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isAppInForegroundApi29(context, packageName)
        } else {
            isAppInForegroundLegacy(context, packageName)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun isAppInForegroundApi29(context: Context, packageName: String): Boolean {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val intervalMillis = 60_000L // Aumentado de 10s a 60s
        val events = usm.queryEvents(now - intervalMillis, now)
        var lastEvent: UsageEvents.Event? = null

        while (events.hasNextEvent()) {
            val event = UsageEvents.Event()
            events.getNextEvent(event)
            if (event.packageName == packageName) {
                lastEvent = event
                Log.d("AppLimit", "🕵 Evento para $packageName: ${event.eventType}")
            }
        }

        return when (lastEvent?.eventType) {
            UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                Log.d("AppLimit", "✅ $packageName está en foreground")
                true
            }
            UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                Log.d("AppLimit", "🚫 $packageName está en background")
                false
            }
            else -> {
                Log.d("AppLimit", "❓ $packageName sin evento claro, usando fallback")
                isAppInForegroundLegacy(context, packageName)
            }
        }
    }


    @SuppressLint("NewApi", "Deprecation")
    private fun isAppInForegroundLegacy(context: Context, packageName: String): Boolean {
        // Método 1: Uso de getRunningAppProcesses (más confiable para apps como Discord)
        try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val runningProcesses = am.runningAppProcesses ?: return false

            for (process in runningProcesses) {
                if (process.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                    process.processName == packageName) {
                    return true
                }
            }
        } catch (e: Exception) {
            Log.e("AppLimit", "Error al verificar procesos", e)
        }

        // Método 2: Uso de UsageStats como fallback
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 10_000, now)

        stats?.firstOrNull { it.packageName == packageName }?.let { recent ->
            return now - recent.lastTimeUsed <= 15_000 // Aumentamos el margen a 15 segundos
        }

        return false
    }

    private fun handleLimitExceeded(context: Context, prefs: SharedPreferences, packageName: String, timeExceeded: Long) {
        Log.d("AppLimit", "🚨 Límite superado para $packageName")
        launchBlocker(context, packageName, timeExceeded)
        prefs.edit()
            .remove("limit_$packageName")
            .remove("start_time_$packageName")
            .remove("accumulated_time_$packageName")
            .apply()
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