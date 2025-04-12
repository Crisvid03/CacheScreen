package com.example.screensense.Modulo2.Bloqueo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screensense.Modulo3.BlockActivity
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
        val apps = mutableListOf<AppInfo>()

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)
        for (ri in resolveInfos) {
            val packageName = ri.activityInfo.packageName
            val appName = ri.loadLabel(pm).toString()
            val appIcon = ri.loadIcon(pm)
            apps.add(AppInfo(packageName, appName, appIcon))
        }

        return apps.sortedBy { it.appName }
    }

    private fun navigateToBlockConfig(appInfo: AppInfo) {
        val intent = Intent(this, BlockActivity::class.java).apply {
            putExtra("package_name", appInfo.packageName)
            putExtra("app_name", appInfo.appName)
        }
        startActivity(intent)
    }
}