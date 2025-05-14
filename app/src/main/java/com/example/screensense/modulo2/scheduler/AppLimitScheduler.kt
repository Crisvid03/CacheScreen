package com.example.screensense.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.screensense.receiver.AppLimitReceiver

object AppLimitScheduler {

    private const val INTERVAL_MILLIS = 10 * 1000L // 10 segundos (ajusta si quieres)

    fun scheduleRepeatingAlarm(context: Context) {

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AppLimitReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Cancelar alarma previa (si existe) para evitar duplicados
        alarmManager.cancel(pendingIntent)

        val triggerAtMillis = System.currentTimeMillis() + INTERVAL_MILLIS

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }

        Log.d("AppLimitScheduler", "⏰ Alarma programada para revisar límites en ${INTERVAL_MILLIS / 1000} segundos.")
    }
}
