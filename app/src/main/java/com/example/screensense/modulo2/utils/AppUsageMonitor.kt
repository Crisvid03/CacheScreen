package com.example.screensense.modulo2.utils

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object AppUsageMonitor {

    // Verificar permiso de acceso a estadísticas de uso
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun isUsageAccessGranted(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Solicitar permiso con diálogo explicativo
    fun showUsageAccessDialog(context: Context) {
        AlertDialog.Builder(context).apply {
            setTitle("Permiso requerido")
            setMessage("Para monitorear el uso de aplicaciones, necesitamos el permiso de 'Acceso a uso de apps'.")
            setPositiveButton("Configuración") { _, _ ->
                openUsageAccessSettings(context)
            }
            setNegativeButton("Cancelar", null)
            setCancelable(false)
            show()
        }
    }

    // Abrir configuración de acceso a uso
    private fun openUsageAccessSettings(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    // Obtener estadísticas de uso en un rango de tiempo
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getUsageStats(
        context: Context,
        interval: Int = UsageStatsManager.INTERVAL_DAILY,
        startTime: Long = getStartOfDay(),
        endTime: Long = System.currentTimeMillis()
    ): List<UsageStats> {
        return try {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            usageStatsManager.queryUsageStats(interval, startTime, endTime)?.filterNotNull() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Obtener estadísticas de uso de hoy
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getTodayUsageStats(context: Context): List<UsageStats> {
        return getUsageStats(context)
    }

    // Obtener tiempo de uso específico para un paquete
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun getUsageTimeForPackage(
        context: Context,
        packageName: String,
        startTime: Long = getStartOfDay(),
        endTime: Long = System.currentTimeMillis()
    ): Long {
        return getUsageStats(context, UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
            .find { it.packageName == packageName }
            ?.totalTimeInForeground ?: 0
    }

    // Obtener inicio del día actual en milisegundos
    private fun getStartOfDay(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    // Formatear tiempo en formato HH:MM:SS
    fun formatDuration(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Obtener nombre de la aplicación desde el package name
    fun getAppName(context: Context, packageName: String): String {
        return try {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    // Formatear fecha legible
    fun formatDate(timestamp: Long, pattern: String = "dd/MM/yyyy HH:mm:ss"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
    }
}