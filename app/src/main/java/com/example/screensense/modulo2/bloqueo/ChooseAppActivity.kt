package com.example.screensense.modulo2.bloqueo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screensense.R
import com.example.screensense.adapter.AppListAdapter
import com.example.screensense.model.AppInfo

class ChooseAppActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_app)

        val recyclerView = findViewById<RecyclerView>(R.id.rv_apps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val appsList = getInstalledApps()
        val adapter = AppListAdapter(appsList) { selectedApp ->
            navigateToBlockConfig(selectedApp)
        }
        recyclerView.adapter = adapter
    }

    private fun getInstalledApps(): List<AppInfo> {
        val pm = packageManager
        val prefs = getSharedPreferences("AppLimits", MODE_PRIVATE)

        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
        val startOfDay = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfDay = System.currentTimeMillis()

        val usageStatsList = usageStatsManager.queryUsageStats(
            android.app.usage.UsageStatsManager.INTERVAL_DAILY, startOfDay, endOfDay
        )

        val apps = mutableListOf<AppInfo>()

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)
        for (ri in resolveInfos) {
            val packageName = ri.activityInfo.packageName
            val appName = ri.loadLabel(pm).toString()
            val appIcon = ri.loadIcon(pm)

            // 🧠 Obtenemos límite y uso
            val limit = prefs.getLong("limit_$packageName", 0L)
            val usageToday = usageStatsList.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L

            // 📦 Creamos el objeto con todos los datos
            val appInfo = AppInfo(appName, packageName, appIcon, limit, usageToday)
            apps.add(appInfo)
        }

        return apps.sortedBy { it.appName }
    }


    private fun navigateToBlockConfig(appInfo: AppInfo) {
        val intent = Intent(this, LimitConfigActivity::class.java).apply {
            putExtra("package_name", appInfo.packageName)
            putExtra("app_name", appInfo.appName)
        }
        startActivity(intent)
    }
}