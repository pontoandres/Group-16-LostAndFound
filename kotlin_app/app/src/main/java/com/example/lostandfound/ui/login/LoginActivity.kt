package com.example.lostandfound.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lostandfound.data.repository.AuthRepositoryImpl
import com.example.lostandfound.databinding.ActivityLoginBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.content.Intent
import com.example.lostandfound.ui.register.RegisterActivity
import com.example.lostandfound.ui.forgotpassword.ForgotPasswordActivity


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.Factory(AuthRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inputs -> ViewModel
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onEmailChanged(s?.toString().orEmpty())
            }
        })
        binding.etPass.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onPasswordChanged(s?.toString().orEmpty())
            }
        })

        // Botón
        binding.btnLogin.setOnClickListener { viewModel.login() }

        // Enlaces (aún sin pantallas)
        binding.txtRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.txtForgot.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Observar estado
        lifecycleScope.launch {
            viewModel.ui.collectLatest { state ->
                binding.progress.visibility = if (state.loading) View.VISIBLE else View.GONE
                binding.btnLogin.isEnabled = !state.loading

                // Errores de campo
                binding.tilEmail.error = null
                binding.tilPass.error = null
                state.error?.let { msg ->
                    // mensaje general (simple). Puedes separar por campo si quieres.
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_SHORT).show()
                }

                if (state.success) {
                    Toast.makeText(this@LoginActivity, "Login OK", Toast.LENGTH_SHORT).show()
                    startActivity(
                        Intent(this@LoginActivity, com.example.lostandfound.ui.home.HomeActivity::class.java)
                    )
                    finish() // cierra login para que no vuelva con back
                }
            }
        }
    }
}
