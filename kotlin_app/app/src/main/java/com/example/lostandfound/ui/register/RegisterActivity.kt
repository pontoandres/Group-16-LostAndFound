package com.example.lostandfound.ui.register

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lostandfound.data.repository.AuthRepositoryImpl
import com.example.lostandfound.databinding.ActivityRegisterBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.widget.doAfterTextChanged


class RegisterActivity: AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val vm: RegisterViewModel by viewModels {
        RegisterViewModel.Factory(AuthRepositoryImpl())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wire inputs
        binding.etEmail.doAfterTextChanged { vm.onEmail(it.toString()) }
        binding.etPass.doAfterTextChanged  { vm.onPass(it.toString()) }
        binding.etName.doAfterTextChanged  { vm.onName(it.toString()) }
        binding.etUniId.doAfterTextChanged { vm.onUniId(it.toString()) }

        binding.btnCreate.setOnClickListener { vm.register() }

        lifecycleScope.launch {
            vm.ui.collectLatest { s ->
                binding.progress.visibility = if (s.loading) View.VISIBLE else View.GONE
                if (s.error != null) Toast.makeText(this@RegisterActivity, s.error, Toast.LENGTH_SHORT).show()
                if (s.success) {
                    Toast.makeText(this@RegisterActivity, "Account created!", Toast.LENGTH_SHORT).show()
                    finish() // vuelve al Login
                }
            }
        }
    }
}
