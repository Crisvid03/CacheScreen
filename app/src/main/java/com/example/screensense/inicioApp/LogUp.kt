package com.example.screensense.inicioApp

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.example.screensense.R
import com.example.screensense.modulo1.ModOneActivityOne
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LogUp : AppCompatActivity() {

    private lateinit var etName: AppCompatEditText
    private lateinit var etMail: AppCompatEditText
    private lateinit var etPassword: AppCompatEditText
    private lateinit var etDate: AppCompatEditText

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // Progress Dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_up)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Inicializar Progress Dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Procesando...")
        progressDialog.setCancelable(false)

        // Referencias a los campos del formulario
        etName = findViewById(R.id.etName)
        etMail = findViewById(R.id.etMail)
        etPassword = findViewById(R.id.etPassword)
        etDate = findViewById(R.id.etDate)

        // DatePicker para seleccionar fecha de nacimiento
        etDate.setOnClickListener { showDatePickerDialog() }

        // Botón NEXT: crear usuario y guardar en Firestore
        val btNext = findViewById<Button>(R.id.btnLogUpNext)
        btNext.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }

        // Botón BACK
        val btnBack = findViewById<Button>(R.id.btnLogUpBack)
        btnBack.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, ModOneActivityOne::class.java)
        startActivity(intent)
    }

    private fun validateInputs(): Boolean {
        val name = etName.text.toString().trim()
        val email = etMail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val birthDate = etDate.text.toString().trim()

        // Validar campos vacíos
        if (name.isEmpty()) {
            etName.error = "El nombre es obligatorio"
            etName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            etMail.error = "El correo es obligatorio"
            etMail.requestFocus()
            return false
        }

        // Validar formato de email
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etMail.error = "Ingresa un correo válido"
            etMail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "La contraseña es obligatoria"
            etPassword.requestFocus()
            return false
        }

        // Validar fortaleza de contraseña
        if (password.length < 6) {
            etPassword.error = "La contraseña debe tener al menos 6 caracteres"
            etPassword.requestFocus()
            return false
        }

        if (birthDate.isEmpty()) {
            etDate.error = "La fecha de nacimiento es obligatoria"
            etDate.requestFocus()
            return false
        }

        // Validar edad (opcional: implementar según requerimientos)
        if (!isValidAge(birthDate)) {
            Toast.makeText(this, "Debes ser mayor de 13 años para registrarte", Toast.LENGTH_SHORT).show()
            etDate.requestFocus()
            return false
        }

        return true
    }

    private fun isValidAge(birthDateStr: String): Boolean {
        try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val birthDate = dateFormat.parse(birthDateStr)
            val calendar = Calendar.getInstance()

            // Guardar fecha actual
            val currentDate = calendar.time

            // Restar 13 años a la fecha actual
            calendar.add(Calendar.YEAR, -13)
            val minDate = calendar.time

            // Comprobar si la fecha de nacimiento indica que el usuario tiene al menos 13 años
            return birthDate?.before(minDate) ?: false
        } catch (e: Exception) {
            Log.e("LogUp", "Error al validar la edad: ${e.message}")
            return false
        }
    }

    private fun registerUser() {
        val name = etName.text.toString().trim()
        val email = etMail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val birthDate = etDate.text.toString().trim()

        // Convertir formato de fecha a ISO para almacenamiento
        val isoDate = convertToISODate(birthDate)

        progressDialog.show() // Mostramos el diálogo con el mensaje "Procesando..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DEBUG", "Usuario registrado correctamente en Auth")
                    saveUserData(name, email, isoDate)
                } else {
                    Log.e("DEBUG", "Error al registrar usuario: ${task.exception?.message}")
                    progressDialog.dismiss()
                    handleAuthError(task.exception)
                }
            }
    }

    private fun convertToISODate(dateStr: String): String {
        try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(dateStr)
            return outputFormat.format(date as Date)
        } catch (e: Exception) {
            Log.e("LogUp", "Error al convertir fecha: ${e.message}")
            return dateStr // Devolver formato original si hay error
        }
    }

    private fun saveUserData(name: String, email: String, birthDate: String) {
        val userId = auth.currentUser?.uid
        val userMap = hashMapOf(
            "name" to name,
            "birthDate" to birthDate,
            "email" to email,
            "createdAt" to Calendar.getInstance().time
        )

        Log.d("DEBUG", "Preparando para guardar datos en Firestore con userId = $userId")

        if (userId == null) {
            Log.e("DEBUG", "Error: userId es null después del registro")
            if (progressDialog.isShowing) progressDialog.dismiss()
            Toast.makeText(this, "Error inesperado. Intenta de nuevo.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").document(userId)
            .set(userMap)
            .addOnSuccessListener {
                Log.d("DEBUG", "Datos guardados correctamente en Firestore")
                if (progressDialog.isShowing) progressDialog.dismiss()
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ModOneActivityOne::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("DEBUG", "Error al guardar en Firestore: ${e.message}")
                if (progressDialog.isShowing) progressDialog.dismiss()
                Toast.makeText(this, "Error al guardar: ${e.message}", Toast.LENGTH_LONG).show()
            }
            .addOnCompleteListener {
                Log.d("DEBUG", "Firestore: operación completada")
                if (progressDialog.isShowing) progressDialog.dismiss()
            }
    }



    private fun handleAuthError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                etPassword.error = "La contraseña es demasiado débil"
                etPassword.requestFocus()
            }
            is FirebaseAuthUserCollisionException -> {
                etMail.error = "Este correo ya está registrado"
                etMail.requestFocus()
            }
            else -> {
                Toast.makeText(this, "Error al registrar: ${exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                etDate.setText(formattedDate)
            },
            year, month, day
        )

        // Establecer fecha máxima (hoy)
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

        datePickerDialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Garantizar que el diálogo se cierra si la actividad se destruye
        if (progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}