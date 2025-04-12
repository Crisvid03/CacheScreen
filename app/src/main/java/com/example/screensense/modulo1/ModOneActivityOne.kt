package com.example.screensense.modulo1

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.screensense.Modulo2.Graficas.DashboardActivity
import com.example.screensense.R
import com.example.screensense.inicioApp.LogUp
import com.google.firebase.auth.FirebaseAuth

class ModOneActivityOne : AppCompatActivity() {

    // Componentes UI
    private lateinit var etMail: AppCompatEditText
    private lateinit var etPassword: AppCompatEditText
    private lateinit var btnLogin: Button
    private lateinit var btnForgotPassword: Button
    private lateinit var btnRegister: Button

    // Firebase Authentication
    private lateinit var auth: FirebaseAuth

    // Diálogo de progreso
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_one_one)

        // Configurar insets para evitar que el contenido se superponga a las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Configurar el ProgressDialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Iniciando sesión...")
        progressDialog.setCancelable(false)

        // Referencias a los elementos de la vista
        etMail = findViewById(R.id.etMailLogIn)
        etPassword = findViewById(R.id.etPasswordLogIn)
        btnLogin = findViewById(R.id.btnLogin)
        btnForgotPassword = findViewById(R.id.btnLogInForgot)
        btnRegister = findViewById(R.id.btnRegister)

        // Listener para el botón de login
        btnLogin.setOnClickListener {
            if (validateInputs()) {
                loginUser()
            }
        }

        // Listener para el botón "olvidé mi contraseña"
        btnForgotPassword.setOnClickListener {
            // Asegúrate de tener creada la actividad de recuperación y declarada en el AndroidManifest.xml
            startActivity(Intent(this, RecuperarPassword1::class.java))
        }

        // Listener para el botón de registro
        btnRegister.setOnClickListener {
            // Navega a la actividad de registro, en este ejemplo, LogUp
            startActivity(Intent(this, LogUp::class.java))
        }
    }

    // Función para validar los inputs del usuario
    private fun validateInputs(): Boolean {
        val email = etMail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty()) {
            etMail.error = "Ingresa tu correo"
            etMail.requestFocus()
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etMail.error = "Ingresa un correo válido"
            etMail.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            etPassword.error = "Ingresa tu contraseña"
            etPassword.requestFocus()
            return false
        }
        return true
    }

    // Función que realiza el proceso de login mediante Firebase Authentication
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
                    Toast.makeText(
                        this,
                        "Alguna credencial es incorrecta",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
