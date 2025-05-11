package com.example.screensense.modulo2.bloqueo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.screensense.R
import java.util.concurrent.TimeUnit

class BlockerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocker)
        setupEdgeToEdge()

        val appName = intent.getStringExtra("app_name") ?: getString(R.string.unknown_app)
        val timeUsed = intent.getLongExtra("time_exceeded", 0)

        setupViews(appName, timeUsed)
    }

    private fun setupEdgeToEdge() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = systemBars.left,
                right = systemBars.right
            )
            insets
        }
    }

    private fun setupViews(appName: String, timeUsed: Long) {
        // Configurar mensaje principal
        findViewById<TextView>(R.id.tv_blocked_message).apply {
            text = getString(R.string.block_message_title, appName)
            contentDescription = getString(R.string.block_message_content_desc, appName)
        }

        // Configurar tiempo excedido
        findViewById<TextView>(R.id.tv_time_exceeded).apply {
            text = getString(R.string.time_exceeded_detail, formatTime(timeUsed))
            contentDescription = getString(R.string.time_exceeded_content_desc, formatTime(timeUsed))
        }

        // Configurar botón
        findViewById<Button>(R.id.btn_ok).apply {
            setOnClickListener { finishAffinity() }
            // Mejorar accesibilidad del botón
            accessibilityTraversalAfter = R.id.tv_time_exceeded
        }
    }

    private fun formatTime(millis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        return when {
            hours > 0 -> String.format("%dh %02dm", hours, minutes)
            minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
            else -> String.format("%ds", seconds)
        }.also {
            // Formato accesible para TalkBack
            it.replace("h", " horas ")
                .replace("m", " minutos ")
                .replace("s", " segundos")
                .trim()
        }
    }

    @Deprecated("Deprecated in superclass", ReplaceWith(""))
    override fun onBackPressed() {
        // Bloqueo completo - no hacer nada
    }

    companion object {
        private const val TAG = "BlockerActivity"
    }
}