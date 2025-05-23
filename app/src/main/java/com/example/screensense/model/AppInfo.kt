package com.example.screensense.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val appName: String,
    val packageName: String,
    val appIcon: Drawable,
    var limitTimeMillis: Long = 0L,
    var usageTodayMillis: Long = 0L
)
