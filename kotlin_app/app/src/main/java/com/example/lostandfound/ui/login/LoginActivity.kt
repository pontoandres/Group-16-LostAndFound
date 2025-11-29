package com.example.lostandfound.ui.login

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.data.repository.AuthRepositoryImpl
import com.example.lostandfound.databinding.ActivityLoginBinding
import com.example.lostandfound.ui.common.BaseActivity
import com.example.lostandfound.ui.forgotpassword.ForgotPasswordActivity
import com.example.lostandfound.ui.home.HomeActivity
import com.example.lostandfound.ui.register.RegisterActivity
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(AuthRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Siempre inflamos primero el layout
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2) Revisar si ya hay sesión activa (después de que Supabase cargue desde storage)
        lifecycleScope.launch {
            // Espera a que Auth termine de inicializarse y leer la sesión guardada
            SupabaseProvider.client.auth.awaitInitialization()

            val session = SupabaseProvider.client.auth.currentSessionOrNull()

            if (session != null) {
                // Ya hay usuario logueado: ir directo al Home
                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                finish()
            } else {
                // No hay sesión: configurar UI de login
                setupLoginUi()
            }
        }
    }

    private fun setupLoginUi() {
        // Inputs -> ViewModel
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                viewModel.onEmailChanged(s?.toString().orEmpty())
            }
        })

        binding.etPass.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {}
            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                viewModel.onPasswordChanged(s?.toString().orEmpty())
            }
        })

        // Botón login
        binding.btnLogin.setOnClickListener { viewModel.login() }

        // Enlace a registro
        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Enlace a "forgot password"
        binding.txtForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Observar estado del ViewModel
        lifecycleScope.launch {
            viewModel.ui.collectLatest { state ->
                binding.progress.visibility =
                    if (state.loading) View.VISIBLE else View.GONE
                binding.btnLogin.isEnabled = !state.loading

                // Limpiar errores de campo
                binding.tilEmail.error = null
                binding.tilPass.error = null

                state.error?.let { msg ->
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                }

                if (state.success) {
                    Toast.makeText(this@LoginActivity, "Login OK", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish() // cierra login para que no vuelva con back
                }
            }
        }
    }
}
