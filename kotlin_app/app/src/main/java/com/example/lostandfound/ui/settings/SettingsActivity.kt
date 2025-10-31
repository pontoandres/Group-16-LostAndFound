package com.example.lostandfound.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivitySettingsBinding
import com.example.lostandfound.ui.common.BaseActivity
import com.example.lostandfound.ui.login.LoginActivity

class SettingsActivity: BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupToolbar()

        val btnBack = binding.btnBack
        val btnSignOut = binding.btnSignOut
        val btnToggleNotifications = binding.btnToggleNotifications

        btnBack.setOnClickListener {
            val intent = Intent(this, com.example.lostandfound.ui.home.HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        btnSignOut.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnToggleNotifications.setOnClickListener {
            Toast.makeText(this, "Feature in progress...", Toast.LENGTH_SHORT).show()
            // TODO: implement enabling/disabling notifications
        }
    }
}