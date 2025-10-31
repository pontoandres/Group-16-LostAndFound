package com.example.lostandfound.ui.claimobject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityClaimObjectBinding
import com.example.lostandfound.ui.common.BaseActivity
import com.google.android.material.appbar.MaterialToolbar

class ClaimObjectActivity : BaseActivity() {

    private lateinit var binding: ActivityClaimObjectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClaimObjectBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val name = intent.getStringExtra("name").orEmpty()
        val description = intent.getStringExtra("description").orEmpty()
        val postedBy = intent.getStringExtra("postedBy").orEmpty()
        val imageRes = intent.getIntExtra("imageRes", 0)

        val tvTitle = binding.tvTitle
        val itemName = name
        tvTitle.text = "Claim $itemName"

        val edtMessage = binding.edtMessage
        val btnSend = binding.btnSend

        btnSend.setOnClickListener {
            val message = edtMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                Toast.makeText(this, "Feature in progress...", Toast.LENGTH_SHORT).show()
                val code = generateClaimCode()
                // TODO: send email + code gen
            } else {
                Toast.makeText(this, "Please enter a message first", Toast.LENGTH_SHORT).show()
            }
        }

    }
    fun generateClaimCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }

}