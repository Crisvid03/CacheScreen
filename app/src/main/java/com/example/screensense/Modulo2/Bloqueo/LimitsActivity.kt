package com.example.screensense.Modulo2.Bloqueo

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.screensense.Modulo2.Graficas.DashboardActivity
import com.example.screensense.Modulo3.BlockActivity
import com.example.screensense.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

class limitsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_limits)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar bottom navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_limits

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_limits -> true
                R.id.nav_block -> {
                    startActivity(Intent(this, BlockActivity::class.java))
                    true
                }
                R.id.nav_usage -> {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Configurar el botón "Añadir Límite de uso"
        val btnAddLimit = findViewById<MaterialButton>(R.id.btn_add_limit)
        btnAddLimit.setOnClickListener {
            // Crear intent para abrir ChooseAppActivity
            val intent = Intent(this, ChooseAppActivity::class.java)
            startActivity(intent)
        }
    }
}