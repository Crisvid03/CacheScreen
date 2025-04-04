package com.example.screensense.inicioApp

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.screensense.R
import java.util.Calendar

class LogUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_log_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etDate = findViewById<AppCompatEditText>(R.id.etDate)
        etDate.setOnClickListener { showDatePickerDialog(etDate) }

        val btnLogUpBack = findViewById<Button>(R.id.btnLogUpBack)
        btnLogUpBack.setOnClickListener {
            navigateUpToMainFive()
        }
    }

    private fun showDatePickerDialog(etDate: AppCompatEditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Formatear la fecha y mostrarla en el EditText
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                etDate.setText(formattedDate)
            },
            year, month, day
        )

        datePickerDialog.show()

    }
    private fun navigateUpToMainFive(){
        val intent = Intent(this , MainFiveActivity::class.java)
        startActivity(intent)
    }
}
