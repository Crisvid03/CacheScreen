package com.example.screensense.modulo2.bloqueo

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.screensense.modulo2.graficas.DashboardActivity
import com.example.screensense.modulo2.utils.AppUsageMonitor
import com.example.screensense.Modulo3.BlockActivity
import com.example.screensense.R
import com.example.screensense.adapter.AppListAdapter
import com.example.screensense.model.AppInfo
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar

class LimitsActivity : AppCompatActivity() {

    private lateinit var adapter: AppListAdapter
    private lateinit var recyclerView: RecyclerView

    private val usageAccessRequest = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkUsagePermissionAndLoadData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_limits)
        setupEdgeToEdge()

        initializeViews()
        setupBottomNavigation()
        setupAddLimitButton()

        checkUsagePermissionAndLoadData()
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.rv_limited_apps)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = AppListAdapter(emptyList()) { appInfo ->
            showAppDetailsDialog(appInfo)
        }
        recyclerView.adapter = adapter
    }

    private fun checkUsagePermissionAndLoadData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!AppUsageMonitor.isUsageAccessGranted(this)) {
                showPermissionRequiredDialog()
                return
            }
        }
        loadLimitedAppsData()
    }

    private fun showPermissionRequiredDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permiso requerido")
            .setMessage("Necesitamos acceso a las estadísticas de uso para mostrar la información de las aplicaciones.")
            .setPositiveButton("Conceder permiso") { _, _ ->
                AppUsageMonitor.showUsageAccessDialog(this)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun loadLimitedAppsData() {
        val sharedPreferences = getSharedPreferences("app_limits", MODE_PRIVATE)
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val todayStats = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppUsageMonitor.getTodayUsageStats(this)
        } else emptyList()

        val limitedApps = apps.mapNotNull { app ->
            val packageName = app.packageName
            if (sharedPreferences.contains("limit_$packageName")) {
                val limit = sharedPreferences.getLong("limit_$packageName", 0L)
                val usage = todayStats.find { it.packageName == packageName }?.totalTimeInForeground ?: 0L

                AppInfo(
                    appName = pm.getApplicationLabel(app).toString(),
                    packageName = packageName,
                    appIcon = pm.getApplicationIcon(app),
                    limitTimeMillis = limit,
                    usageTodayMillis = usage
                )
            } else null
        }

        if (limitedApps.isEmpty()) {
            showEmptyStateMessage()
        } else {
            adapter = AppListAdapter(limitedApps) { appInfo ->
                showAppDetailsDialog(appInfo)
            }
            recyclerView.adapter = adapter
        }
    }

    private fun showEmptyStateMessage() {
        Snackbar.make(
            findViewById(R.id.main),
            "No hay aplicaciones con límites configurados",
            Snackbar.LENGTH_LONG
        ).setAction("Añadir") {
            startActivity(Intent(this, ChooseAppActivity::class.java))
        }.show()
    }

    private fun showAppDetailsDialog(appInfo: AppInfo) {
        AlertDialog.Builder(this)
            .setTitle(appInfo.appName)
            .setMessage(
                """
            Uso hoy: ${AppUsageMonitor.formatDuration(appInfo.usageTodayMillis)}
            Límite diario: ${AppUsageMonitor.formatDuration(appInfo.limitTimeMillis)}
            Restante: ${AppUsageMonitor.formatDuration(appInfo.limitTimeMillis - appInfo.usageTodayMillis)}
            """.trimIndent()
            )
            .setPositiveButton("Editar límite") { _, _ ->
                openLimitConfig(appInfo)
            }
            .setNeutralButton("Eliminar límite") { _, _ ->
                removeLimit(appInfo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    
    private fun removeLimit(appInfo: AppInfo) {
        val sharedPreferences = getSharedPreferences("app_limits", MODE_PRIVATE)
        sharedPreferences.edit().remove("limit_${appInfo.packageName}").apply()

        Snackbar.make(
            findViewById(R.id.main),
            "Se eliminó el límite de ${appInfo.appName}",
            Snackbar.LENGTH_SHORT
        ).show()

        loadLimitedAppsData() // Recargar lista actualizada
    }

    private fun openLimitConfig(appInfo: AppInfo) {
        val intent = Intent(this, LimitConfigActivity::class.java).apply {
            putExtra("app_name", appInfo.appName)
            putExtra("package_name", appInfo.packageName)
        }
        startActivity(intent)
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_limits

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_limits -> true
                R.id.nav_block -> {
                    startActivity(Intent(this, BlockActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_usage -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupAddLimitButton() {
        findViewById<MaterialButton>(R.id.btn_add_limit).setOnClickListener {
            startActivity(Intent(this, ChooseAppActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (AppUsageMonitor.isUsageAccessGranted(this)) {
                loadLimitedAppsData()
            }
        }
    }
}