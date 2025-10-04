package com.example.lostandfound.ui.forgotpassword

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.lostandfound.databinding.ActivityForgotPasswordBinding
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeState()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnRecover.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            viewModel.sendReset(email) // ✅ MVVM: delega al VM
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is ForgotResetState.Idle -> {
                            binding.btnRecover.isEnabled = true
                        }
                        is ForgotResetState.Loading -> {
                            binding.btnRecover.isEnabled = false
                        }
                        is ForgotResetState.Success -> {
                            binding.btnRecover.isEnabled = true
                            Toast.makeText(this@ForgotPasswordActivity, state.msg, Toast.LENGTH_LONG).show()
                            finish()
                        }
                        is ForgotResetState.Error -> {
                            binding.btnRecover.isEnabled = true
                            Toast.makeText(this@ForgotPasswordActivity, state.msg, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}