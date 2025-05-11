package com.example.screensense.modulo2.graficas

import com.github.mikephil.charting.formatter.ValueFormatter

class DayAxisFormatter : ValueFormatter() {
    private val days = arrayOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    override fun getFormattedValue(value: Float): String {
        val index = value.toInt()
        return if (index in days.indices) days[index] else ""
    }
}
