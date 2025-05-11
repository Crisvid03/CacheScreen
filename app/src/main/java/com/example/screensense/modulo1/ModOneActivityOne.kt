package com.example.screensense.modulo1

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.example.screensense.modulo2.graficas.DashboardActivity
import com.example.screensense.R
import com.example.screensense.inicioApp.LogUp
import com.google.firebase.auth.FirebaseAuth

class ModOneActivityOne : AppCompatActivity() {

    private lateinit var etMail: AppCompatEditText
    private lateinit var etPassword: AppCompatEditText
    private lateinit var btnLogin: Button
    private lateinit var btnForgotPassword: Button
    private lateinit var btnRegister: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mod_one_one)

        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this).apply {
            setMessage("Iniciando sesión...")
            setCancelable(false)
        }

        // Configuración del EditText de Email con teclado optimizado
        etMail = findViewById<AppCompatEditText>(R.id.etMailLogIn).apply {
            inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or InputType.TYPE_CLASS_TEXT
            isFocusable = true
            isFocusableInTouchMode = true
        }

        etPassword = findViewById<AppCompatEditText>(R.id.etPasswordLogIn).apply {
            isFocusable = true
            isFocusableInTouchMode = true
        }

        btnLogin = findViewById(R.id.btnLogin)
        btnForgotPassword = findViewById(R.id.btnLogInForgot)
        btnRegister = findViewById(R.id.btnRegister)

        btnLogin.setOnClickListener { if (validateInputs()) loginUser() }
        btnForgotPassword.setOnClickListener { startActivity(Intent(this, RecuperarPassword1::class.java)) }
        btnRegister.setOnClickListener { startActivity(Intent(this, LogUp::class.java)) }
    }

    private fun validateInputs(): Boolean {
        val email = etMail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        when {
            email.isEmpty() -> {
                etMail.error = "Ingresa tu correo"
                etMail.requestFocus()
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etMail.error = "Ingresa un correo válido"
                etMail.requestFocus()
                return false
            }
            password.isEmpty() -> {
                etPassword.error = "Ingresa tu contraseña"
                etPassword.requestFocus()
                return false
            }
            else -> return true
        }
    }

    private fun loginUser() {
        val email = etMail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        progressDialog.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_LONG).show()
                }
            }
    }
}