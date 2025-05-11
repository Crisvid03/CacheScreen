package com.example.screensense.modulo2.graficas

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.screensense.Modulo3.BlockActivity
import com.example.screensense.R
import com.example.screensense.modulo2.bloqueo.LimitsActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.TimeUnit

class DashboardActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart

    // Tags para los logs
    private companion object {
        const val TAG = "DashboardActivity"
        const val DEBUG_TOP_APPS = "DEBUG_APPS_TOP"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        barChart = findViewById(R.id.barChartWeekly)

        // Verificar y solicitar permiso de acceso a uso
        if (!tieneAccesoUso()) {
            solicitarPermisoAccesoUso()
        } else {
            // Solo proceder si tenemos permiso
            configurarGraficoBarras()
            val appsTop = obtenerTop3Apps()
            mostrarTop3Apps(appsTop)
        }


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_usage // Estás en la pantalla de "Mi uso"

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_usage -> {
                    // Ya estás en esta pantalla, no hagas nada
                    true
                }
                R.id.nav_limits -> {
                    startActivity(Intent(this, LimitsActivity::class.java))
                    true
                }
                R.id.nav_block -> {
                    startActivity(Intent(this, BlockActivity::class.java))
                    true
                }
                else -> false
            }
        }



    }

    /**
     * Configura el gráfico de barras con los datos de uso diario
     */
    private fun configurarGraficoBarras() {
        val datosUso = obtenerUsoDiarioTotal()
        val entradas = datosUso.mapIndexed { indice, (_, horas) ->
            BarEntry(indice.toFloat(), horas)
        }

        val conjuntoDatos = BarDataSet(entradas, "Uso diario (horas)").apply {
            setColors(*ColorTemplate.MATERIAL_COLORS)
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }

        val datos = BarData(conjuntoDatos)
        barChart.data = datos

        val ejeX = barChart.xAxis
        ejeX.position = XAxis.XAxisPosition.BOTTOM
        ejeX.setDrawGridLines(false)
        ejeX.textColor = Color.WHITE
        ejeX.labelCount = 7
        ejeX.valueFormatter = IndexAxisValueFormatter(datosUso.map { it.first })

        barChart.axisLeft.textColor = Color.WHITE
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.legend.textColor = Color.WHITE
        barChart.setFitBars(true)
        barChart.invalidate()
    }

    /**
     * Obtiene el uso total por día de la última semana
     */
    private fun obtenerUsoDiarioTotal(): List<Pair<String, Float>> {
        val gestorEstadisticas = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendario = java.util.Calendar.getInstance()
        val finTiempo = calendario.timeInMillis
        calendario.add(java.util.Calendar.DAY_OF_YEAR, -6)
        val inicioTiempo = calendario.timeInMillis

        val listaEstadisticas = gestorEstadisticas.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            inicioTiempo,
            finTiempo
        ) ?: return emptyList()

        val usoPorDia = mutableMapOf<String, Long>()
        for (estadistica in listaEstadisticas) {
            val fecha = java.util.Calendar.getInstance().apply {
                timeInMillis = estadistica.firstTimeStamp
            }
            val diaSemana = android.text.format.DateFormat.format("EEE", fecha).toString()
            usoPorDia[diaSemana] = (usoPorDia[diaSemana] ?: 0L) + estadistica.totalTimeInForeground
        }

        val dias = mutableListOf<String>()
        val tempCalendario = java.util.Calendar.getInstance()
        tempCalendario.timeInMillis = inicioTiempo
        for (i in 0..6) {
            val dia = android.text.format.DateFormat.format("EEE", tempCalendario).toString()
            dias.add(dia)
            tempCalendario.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }

        return dias.map { dia ->
            val usoMs = usoPorDia[dia] ?: 0L
            val horasUso = (usoMs.toFloat() / TimeUnit.HOURS.toMillis(1)).let {
                (it * 10).toInt() / 10f // Redondear a 1 decimal
            }
            dia to horasUso
        }
    }

    /**
     * Muestra las 3 apps más usadas en la interfaz
     */
    private fun mostrarTop3Apps(apps: List<AppUsoTop>) {
        val contenedor = findViewById<LinearLayout>(R.id.topThreeAppsContainer)
        contenedor.removeAllViews()

        // Configuración de diseño para cada app
        val parametrosLayout = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(32, 16, 32, 16) // Más margen para mejor visualización
        }

        apps.forEach { app ->
            val layoutApp = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = parametrosLayout
            }

            // ImageView para el icono
            ImageView(this).apply {
                try {
                    // Obtener el icono de forma más robusta
                    val icono = if (app.iconoApp != null) {
                        app.iconoApp
                    } else {
                        // Fallback si no se obtiene el icono
                        resources.getDrawable(R.drawable.ic_default_app, theme)
                    }

                    setImageDrawable(icono)
                    layoutParams = LinearLayout.LayoutParams(
                        resources.getDimensionPixelSize(R.dimen.icon_size), // 120dp
                        resources.getDimensionPixelSize(R.dimen.icon_size)  // 120dp
                    )
                    adjustViewBounds = true
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    layoutApp.addView(this)
                } catch (e: Exception) {
                    Log.e(TAG, "Error al cargar icono para ${app.nombreApp}", e)
                }
            }

            // TextView para el nombre
            TextView(this).apply {
                text = app.nombreApp
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(0, 8, 0, 0)
                textSize = 14f
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                layoutApp.addView(this)
            }

            // TextView para el tiempo
            TextView(this).apply {
                text = app.tiempoUso
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                textSize = 12f
                layoutApp.addView(this)
            }

            contenedor.addView(layoutApp)
        }
    }

    /**
     * Obtiene las 3 aplicaciones más usadas
     */
    private fun obtenerTop3Apps(): List<AppUsoTop> {
        val listaEstadisticas = obtenerEstadisticasUsoUltimaSemana()
        val pm = packageManager
        val mapaUso = mutableMapOf<String, Long>()

        // Sumar tiempo de uso por paquete
        for (estadistica in listaEstadisticas) {
            if (estadistica.totalTimeInForeground > 0) {
                mapaUso[estadistica.packageName] = mapaUso.getOrDefault(estadistica.packageName, 0L) + estadistica.totalTimeInForeground
            }
        }

        Log.d(DEBUG_TOP_APPS, "Total paquetes con uso > 0: ${mapaUso.size}")

        val listaAppsUso = mutableListOf<InfoAppUso>()
        val listaNegraSistema = setOf(
            "com.android.systemui",
            "com.sec.android.app.launcher",
            "com.google.android.gms",
            "com.android.settings",
            "com.samsung.android.bixby.agent",
            packageName // Excluir esta app
        )

        for ((nombrePaqueteRaw, tiempoUso) in mapaUso) {
            val nombrePaquete = nombrePaqueteRaw.split(":").first()

            try {
                val infoApp = pm.getApplicationInfo(nombrePaquete, 0)
                if (nombrePaquete in listaNegraSistema) {
                    Log.d(DEBUG_TOP_APPS, "⛔ App de sistema filtrada: $nombrePaquete")
                    continue
                }

                // Solo incluir apps que se pueden iniciar (tienen intent MAIN/LAUNCHER)
                val intentInicio = pm.getLaunchIntentForPackage(nombrePaquete)
                if (intentInicio == null) {
                    Log.d(DEBUG_TOP_APPS, "⛔ App no iniciable: $nombrePaquete")
                    continue
                }

                val nombreApp = pm.getApplicationLabel(infoApp).toString()
                val iconoApp = pm.getApplicationIcon(nombrePaquete)
                listaAppsUso.add(InfoAppUso(nombrePaquete, nombreApp, tiempoUso, iconoApp))
                Log.d(DEBUG_TOP_APPS, "✅ App válida: $nombreApp ($nombrePaquete) - Tiempo: ${tiempoUso}ms")

            } catch (e: PackageManager.NameNotFoundException) {
                Log.w(DEBUG_TOP_APPS, "⚠️ App no instalada: $nombrePaquete")
            } catch (e: Exception) {
                Log.e(DEBUG_TOP_APPS, "❌ Error con $nombrePaquete: ${e.message}")
            }
        }

        return listaAppsUso
            .sortedByDescending { it.tiempoUsoMillis }
            .take(3)
            .map {
                AppUsoTop(
                    nombreApp = it.nombreApp,
                    iconoApp = it.iconoApp,
                    tiempoUso = formatearTiempoUso(it.tiempoUsoMillis)
                )
            }
    }

    /**
     * Formatea el tiempo de uso de milisegundos a formato "Xh Ym"
     */
    private fun formatearTiempoUso(tiempoMillis: Long): String {
        val horas = TimeUnit.MILLISECONDS.toHours(tiempoMillis)
        val minutos = TimeUnit.MILLISECONDS.toMinutes(tiempoMillis) % 60
        return "${horas}h ${minutos}m"
    }

    /**
     * Obtiene las estadísticas de uso de la última semana
     */
    private fun obtenerEstadisticasUsoUltimaSemana(): List<UsageStats> {
        val gestorEstadisticas = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val calendario = java.util.Calendar.getInstance()
        val finTiempo = calendario.timeInMillis
        calendario.add(java.util.Calendar.DAY_OF_YEAR, -7)
        val inicioTiempo = calendario.timeInMillis

        return gestorEstadisticas.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            inicioTiempo,
            finTiempo
        ) ?: emptyList()
    }

    /**
     * Solicita permiso de acceso a estadísticas de uso
     */
    private fun solicitarPermisoAccesoUso() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        Toast.makeText(this, "Por favor, activa el acceso a estadísticas de uso para esta app", Toast.LENGTH_LONG).show()
    }

    /**
     * Verifica si la app tiene permiso de acceso a estadísticas de uso
     */
    private fun tieneAccesoUso(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val modo = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName
        )
        return modo == AppOpsManager.MODE_ALLOWED
    }
}

// Clase de datos para apps top
data class AppUsoTop(
    val nombreApp: String,
    val iconoApp: android.graphics.drawable.Drawable,
    val tiempoUso: String
)

// Clase de datos para información de uso de apps
data class InfoAppUso(
    val nombrePaquete: String,
    val nombreApp: String,
    val tiempoUsoMillis: Long,
    val iconoApp: android.graphics.drawable.Drawable
)