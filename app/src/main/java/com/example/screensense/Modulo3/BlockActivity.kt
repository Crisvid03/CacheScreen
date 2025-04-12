package com.example.screensense.Modulo3

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.screensense.Modulo2.Bloqueo.limitsActivity
import com.example.screensense.Modulo2.Graficas.DashboardActivity
import com.example.screensense.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class BlockActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_block)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_block // Estás en la pantalla de "blok"

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_block -> {
                    // Ya estás en esta pantalla
                    true
                }
                R.id.nav_limits -> {
                    startActivity(Intent(this, limitsActivity::class.java))
                    true
                }
                R.id.nav_usage -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}