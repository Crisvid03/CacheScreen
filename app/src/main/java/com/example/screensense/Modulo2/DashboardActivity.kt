package com.example.screensense.Modulo2


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.screensense.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import android.content.Context
import android.provider.Settings
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.util.Log



import android.app.AppOpsManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class DashboardActivity : AppCompatActivity() {

    private fun getUsageStatsLast7Days(): List<UsageStats> {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - (7 * 24 * 60 * 60 * 1000) // Últimos 7 días

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )

        // Filtramos los que tienen tiempo de uso mayor a 0
        val filteredList = usageStatsList.filter { it.totalTimeInForeground > 0 }

        // Ordenamos de mayor a menor tiempo de uso
        val sortedList = filteredList.sortedByDescending { it.totalTimeInForeground }

        Log.d("USAGE_STATS", "Top apps en uso: ${sortedList.size}")
        return sortedList
    }


    private lateinit var barChart: BarChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        requestUsageAccessPermission()

        barChart = findViewById(R.id.barChartWeekly)

        setupBarChartConDatosReales()
    }

    private fun setupBarChart() {
        TODO("Not yet implemented")
    }

    private fun requestUsageAccessPermission() {
        if (!hasUsageAccess()) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.data = Uri.parse("package:$packageName")
            startActivity(intent)
            Toast.makeText(this, "Por favor, concede el permiso de acceso a uso", Toast.LENGTH_LONG).show()
        }
    }


    private fun hasUsageAccess(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            "android:get_usage_stats",
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun setupBarChartConDatosReales() {
        val usageStatsList = getUsageStatsLast7Days()

        val entries = mutableListOf<BarEntry>()
        val appNames = mutableListOf<String>()

        for ((index, stat) in usageStatsList.take(7).withIndex()) {
            val usageHours = stat.totalTimeInForeground / 1000f / 60f / 60f // ms -> h
            entries.add(BarEntry(index.toFloat(), usageHours))

            // Nombre del paquete, luego se puede convertir en nombre real
            appNames.add(stat.packageName)
        }

        val dataSet = BarDataSet(entries, "Horas de uso")
        dataSet.setColors(*ColorTemplate.MATERIAL_COLORS)
        dataSet.valueTextColor = android.graphics.Color.WHITE
        dataSet.valueTextSize = 12f

        val data = BarData(dataSet)
        barChart.data = data

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.textColor = android.graphics.Color.WHITE
        xAxis.labelCount = appNames.size
        xAxis.valueFormatter = IndexAxisValueFormatter(appNames)

        barChart.axisLeft.textColor = android.graphics.Color.WHITE
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.textColor = android.graphics.Color.WHITE
        barChart.setFitBars(true)
        barChart.invalidate()
    }

}
