package com.example.screensense.modulo2.bloqueo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.screensense.R
import com.example.screensense.modulo2.utils.PermissionUtils
import com.example.screensense.receiver.AppLimitReceiver

class LimitConfigActivity : AppCompatActivity() {

    private lateinit var packageName: String
    private lateinit var appName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_limit_config)

        appName = intent.getStringExtra("app_name") ?: run {
            showErrorAndFinish("Error: Nombre de app no disponible")
            return
        }

        packageName = intent.getStringExtra("package_name") ?: run {
            showErrorAndFinish("Error: Package name no disponible")
            return
        }

        initUI()
        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (PermissionUtils.checkUsageStatsPermission(this)) {
            setupLimitConfiguration()
        }
    }

    private fun initUI() {
        val appIconImageView: ImageView = findViewById(R.id.iv_app_icon)
        val appNameTextView: TextView = findViewById(R.id.tv_app_name)
        val npHours: NumberPicker = findViewById(R.id.np_hours)
        val npMinutes: NumberPicker = findViewById(R.id.np_minutes)
        val btnSave: Button = findViewById(R.id.btn_save_limit)

        try {
            appIconImageView.setImageDrawable(packageManager.getApplicationIcon(packageName))
        } catch (e: PackageManager.NameNotFoundException) {
            appIconImageView.setImageResource(R.drawable.ic_default_app)
        }

        appNameTextView.text = appName

        npHours.apply {
            minValue = 0
            maxValue = 12
            value = 1
            wrapSelectorWheel = true
        }

        npMinutes.apply {
            minValue = 0
            maxValue = 59
            wrapSelectorWheel = true
        }

        btnSave.setOnClickListener { saveLimit() }
    }

    private fun checkPermissions() {
        if (!PermissionUtils.checkUsageStatsPermission(this)) {
            showPermissionExplanationDialog()
        } else {
            setupLimitConfiguration()
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permiso requerido")
            .setMessage("Para monitorear el tiempo de uso, necesitamos acceso a las estadísticas de uso de aplicaciones.")
            .setPositiveButton("Configurar") { _, _ ->
                PermissionUtils.requestUsageStatsPermission(this)
            }
            .setNegativeButton("Cancelar") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun setupLimitConfiguration() {
        findViewById<Button>(R.id.btn_save_limit).isEnabled = true
    }

    private fun saveLimit() {
        val npHours: NumberPicker = findViewById(R.id.np_hours)
        val npMinutes: NumberPicker = findViewById(R.id.np_minutes)

        val hours = npHours.value
        val minutes = npMinutes.value

        if (hours == 0 && minutes == 0) {
            Toast.makeText(this, "Establece un límite mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        val totalMillis = (hours * 3600000L) + (minutes * 60000L)

        getSharedPreferences("app_limits", MODE_PRIVATE).edit().apply {
            putLong("limit_$packageName", totalMillis)
            putLong("start_time_$packageName", System.currentTimeMillis())
            apply()
        }

        // 🚨 Programar la alarma general
        com.example.screensense.scheduler.AppLimitScheduler.scheduleRepeatingAlarm(this)

        val intent = Intent(this, LimitsActivity::class.java)
        startActivity(intent)

        Toast.makeText(this, "Límite guardado para $appName", Toast.LENGTH_SHORT).show()
        finish()
    }


    private fun scheduleLimitCheck(packageName: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AppLimitReceiver::class.java).apply {
            putExtra("package_name", packageName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            packageName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + 60_000 // en 1 minuto arranca el chequeo

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            60_000,  // cada minuto
            pendingIntent
        )
    }

    private fun showErrorAndFinish(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish()
    }
}
