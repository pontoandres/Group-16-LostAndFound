package com.example.lostandfound.ui.resetpassword

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lostandfound.databinding.ActivityResetPasswordBinding
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private val viewModel: ResetPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Procesar deeplink inicial
        intent.dataString?.let { viewModel.importSessionFromUrl(it) }

        // 2) UI
        binding.btnChangePassword.setOnClickListener {
            val newPass = binding.etNewPassword.text.toString().trim()
            viewModel.updatePassword(newPass)
        }

        // 3) Observa estado
        observeState()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Si la Activity ya estaba abierta y llegó el deeplink
        intent.dataString?.let { viewModel.importSessionFromUrl(it) }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is ResetState.Idle -> {
                            binding.btnChangePassword.isEnabled = true
                        }
                        is ResetState.Loading -> {
                            binding.btnChangePassword.isEnabled = false
                        }
                        is ResetState.Success -> {
                            binding.btnChangePassword.isEnabled = true
                            Toast.makeText(this@ResetPasswordActivity, state.msg, Toast.LENGTH_LONG).show()
                            finish()
                        }
                        is ResetState.Error -> {
                            binding.btnChangePassword.isEnabled = true
                            Toast.makeText(this@ResetPasswordActivity, state.msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}