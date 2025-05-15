package com.example.screensense.modulo2.bloqueo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.screensense.R

class BlockerService : Service() {

    private val CHANNEL_ID = "blocker_channel" // Definir el ID del canal como constante

    override fun onCreate() {
        super.onCreate()
        Log.d("BlockerService", "🧩 Servicio creado")
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, // Usar la constante aquí
                "Canal de Bloqueo",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Canal para notificaciones de bloqueo de apps"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("BlockerService", "🚀 onStartCommand ejecutado")
        val appName = intent?.getStringExtra("app_name") ?: "App desconocida"
        val timeExceeded = intent?.getLongExtra("time_exceeded", 0L) ?: 0L

        // Crear la notificación
        val notification = NotificationCompat.Builder(this, CHANNEL_ID) // Usar la constante aquí
            .setContentTitle("Bloqueando $appName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        // Usar un ID dinámico para evitar conflictos si hay múltiples bloqueos
        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        startForeground(notificationId, notification)

        // Verificar si el dispositivo está activo
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isInteractive = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            powerManager.isInteractive
        } else {
            powerManager.isScreenOn
        }

        if (isInteractive) {
            try {
                val blockerIntent = Intent(this, BlockerActivity::class.java).apply {
                    putExtra("app_name", appName)
                    putExtra("time_exceeded", timeExceeded)
                    this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Corregido: 'this.' añadido
                }

                Log.d("BlockerService", "Intentando abrir BlockerActivity para $appName")

                try {
                    startActivity(blockerIntent)
                } catch (e: Exception) {
                    Log.e("BlockerService", "Error al abrir BlockerActivity", e)
                }


                startActivity(blockerIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                // Puedes mostrar un mensaje o notificación adicional aquí si falla
            }
        } else {
            // Aquí podrías guardar en un log o enviar una notificación para abrir después
        }

        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
