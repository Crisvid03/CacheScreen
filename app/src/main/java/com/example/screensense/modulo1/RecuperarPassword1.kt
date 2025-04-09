package com.example.screensense.modulo1

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.example.screensense.R
import com.google.firebase.auth.FirebaseAuth

class RecuperarPassword1 : AppCompatActivity() {

    private lateinit var etEmail: AppCompatEditText
    private lateinit var btnSend: Button
    private lateinit var progressDialog: ProgressDialog
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recuperar_password1)

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Inicializa views
        etEmail = findViewById(R.id.etCorreoRecuperar)
        btnSend = findViewById(R.id.btnEnviarPin)

        // Inicializa ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Buscando correo...")
        progressDialog.setCancelable(false)

        btnSend.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Ingresa un correo"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Correo no válido"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            Log.d("DEBUG_EMAIL", "Correo ingresado: '$email'")
            progressDialog.show()

            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    progressDialog.dismiss()

                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods

                        // Más específico en los logs
                        if (signInMethods == null) {
                            Log.d("DEBUG_SIGNIN", "signInMethods es null")
                        } else {
                            Log.d("DEBUG_SIGNIN", "Métodos encontrados: $signInMethods")
                            if (signInMethods.isEmpty()) {
                                Log.d("DEBUG_SIGNIN", "signInMethods está vacío")
                            } else {
                                Log.d("DEBUG_SIGNIN", "Métodos disponibles: ${signInMethods.joinToString()}")
                            }
                        }

                        // Simplifica la lógica
                        if (signInMethods != null && signInMethods.isNotEmpty()) {
                            Toast.makeText(this, "✅ Correo encontrado", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "❌ Correo NO registrado", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("DEBUG_SIGNIN", "Error: ${task.exception?.message}")
                        Toast.makeText(this, "Error al buscar el correo", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
