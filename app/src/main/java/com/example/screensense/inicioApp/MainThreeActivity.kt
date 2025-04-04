package com.example.screensense.inicioApp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.screensense.R

class MainThreeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main_three)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnMainThreeNext = findViewById<Button>(R.id.btnMainThreeNext)
        btnMainThreeNext.setOnClickListener{
            navigateToMainFour()
        }

        val btnMainThreeBack = findViewById<Button>(R.id.btnMainThreeBack)
        btnMainThreeBack.setOnClickListener {
            navigateToMainTwo()
        }

    }

    private fun navigateToMainFour() {
        val intent = Intent(this , MainFourActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMainTwo() {
        val intent = Intent(this , MainTwoActivity::class.java)
        startActivity(intent)
    }

}