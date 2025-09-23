package com.example.lostandfound.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.lostandfound.R
import com.example.lostandfound.ui.common.BaseActivity
import com.example.lostandfound.ui.login.LoginActivity

class SettingsActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupToolbar()

        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnSignOut = findViewById<Button>(R.id.btnSignOut)
        val btnToggleNotifications = findViewById<Button>(R.id.btnToggleNotifications)

        btnBack.setOnClickListener {
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