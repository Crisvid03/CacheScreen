package com.example.screensense.modulo2

import android.app.Application
import android.util.Log
import com.example.screensense.scheduler.AppLimitScheduler

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("AppDebug", "🚫 La app NO tiene permiso para programar alarmas exactas.")
            } else {
                Log.d("AppDebug", "✅ La app tiene permiso para programar alarmas exactas.")
            }
        }


        Log.d("AppDebug", "✅ App se ha iniciado - onCreate ejecutado")

        // Programar la alarma de verificación de límites
        AppLimitScheduler.scheduleRepeatingAlarm(this)
    }
}