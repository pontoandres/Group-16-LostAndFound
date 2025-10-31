package com.example.lostandfound.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityNotificationsBinding
import com.example.lostandfound.ui.common.BaseActivity

class NotificationsActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupToolbar()

        val btnBack = binding.btnBack
        btnBack.setOnClickListener {
            val intent = Intent(this, com.example.lostandfound.ui.home.HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        val rv = binding.rvNotifications
        rv.layoutManager = LinearLayoutManager(this)

        val notifications = listOf(
            "You have a new email!",
            "Post successfully created",
            "Object claimed!",
            "You have a new email!"
        )

        rv.adapter = NotificationsAdapter(notifications)
    }
}