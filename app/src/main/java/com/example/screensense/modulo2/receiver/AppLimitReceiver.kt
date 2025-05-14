package com.example.screensense.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.screensense.modulo2.utils.AppLimit

class AppLimitReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("AppLimitReceiver", "⏰ Alarma recibida - ejecutando checkAppLimits()")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            com.example.screensense.modulo2.utils.AppLimit.checkAppLimits(context)
        }

        // 🔁 Reprogramar la alarma para dentro de 15 min
        com.example.screensense.scheduler.AppLimitScheduler.scheduleRepeatingAlarm(context)
    }

}