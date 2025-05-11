package com.example.screensense.modulo2.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.screensense.R
import com.example.screensense.modulo2.bloqueo.BlockerActivity

object AppLimit {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun checkAppLimits(context: Context) {
        val prefs = context.getSharedPreferences("AppLimits", Context.MODE_PRIVATE)
        val allEntries = prefs.all

        for ((key, value) in allEntries) {
            if (key.startsWith("limit_")) {
                val packageName = key.removePrefix("limit_")
                val limitMillis = value as? Long ?: continue
                val startTime = prefs.getLong("start_time_$packageName", 0)

                if (startTime > 0 && System.currentTimeMillis() - startTime >= limitMillis) {
                    launchBlocker(context, packageName, System.currentTimeMillis() - startTime)
                    prefs.edit().remove("limit_$packageName").remove("start_time_$packageName").apply()
                }
            }
        }
    }

    private fun launchBlocker(context: Context, packageName: String, timeExceeded: Long) {
        val appName = try {
            context.packageManager.getApplicationLabel(
                context.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            ).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            context.getString(R.string.unknown_app)
        }

        Intent(context, BlockerActivity::class.java).apply {
            putExtra("app_name", appName)
            putExtra("time_exceeded", timeExceeded)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(this)
        }
    }

    fun setAppLimit(context: Context, packageName: String, hours: Int, minutes: Int) {
        val limitMillis = (hours * 3_600_000L) + (minutes * 60_000L)
        context.getSharedPreferences("AppLimits", Context.MODE_PRIVATE).edit()
            .putLong("limit_$packageName", limitMillis)
            .putLong("start_time_$packageName", System.currentTimeMillis())
            .apply()
    }

    fun removeAppLimit(context: Context, packageName: String) {
        context.getSharedPreferences("AppLimits", Context.MODE_PRIVATE).edit()
            .remove("limit_$packageName")
            .remove("start_time_$packageName")
            .apply()
    }

    fun getRemainingTime(context: Context, packageName: String): Long {
        val prefs = context.getSharedPreferences("AppLimits", Context.MODE_PRIVATE)
        val limit = prefs.getLong("limit_$packageName", 0)
        val startTime = prefs.getLong("start_time_$packageName", 0)

        return if (limit > 0 && startTime > 0) {
            limit - (System.currentTimeMillis() - startTime)
        } else {
            0
        }
    }
}