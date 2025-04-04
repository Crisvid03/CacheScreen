package com.example.screensense.inicioApp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.screensense.R

class MainFourActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_four)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Botones de Acceso
        val btnMainFourNext = findViewById<Button>(R.id.btnMainFourNext)
        btnMainFourNext.setOnClickListener {
            navigateUpToMainFive()
        }

        val btnMainFourBack = findViewById<Button>(R.id.btnMainFourBack)
        btnMainFourBack.setOnClickListener {
            navigateUpToMainThree()
        }
    }

    private fun navigateUpToMainFive() {
        val intent = Intent(this , MainFiveActivity::class.java)
        startActivity(intent)
    }

    private fun navigateUpToMainThree() {
        val intent = Intent(this , MainThreeActivity::class.java)
        startActivity(intent)
    }
}