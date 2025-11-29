package com.example.lostandfound.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.databinding.ActivitySettingsBinding
import com.example.lostandfound.ui.common.BaseActivity
import com.example.lostandfound.ui.home.HomeActivity
import com.example.lostandfound.ui.login.LoginActivity
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Drawer + toolbar
        setupToolbar()

        val btnBack = binding.btnBack
        val btnSignOut = binding.btnSignOut
        val btnToggleNotifications = binding.btnToggleNotifications

        // Volver a Home
        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }

        // 🔹 Cerrar sesión REAL
        btnSignOut.setOnClickListener {
            lifecycleScope.launch {
                try {
                    // 1) Cerrar sesión en Supabase (borra tokens/cookie local)
                    SupabaseProvider.client.auth.signOut()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@SettingsActivity,
                        "Error signing out: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // 2) Ir a Login y limpiar el back stack
                val intent = Intent(this@SettingsActivity, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
        }

        // Placeholder de notificaciones
        btnToggleNotifications.setOnClickListener {
            Toast.makeText(this, "Feature in progress...", Toast.LENGTH_SHORT).show()
        }
    }
}
