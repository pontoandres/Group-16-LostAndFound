package com.example.lostandfound.ui.claimobject

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfound.R
import com.example.lostandfound.ui.common.BaseActivity
import com.google.android.material.appbar.MaterialToolbar

class ClaimObjectActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_claim_object)

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val name = intent.getStringExtra("name").orEmpty()
        val description = intent.getStringExtra("description").orEmpty()
        val postedBy = intent.getStringExtra("postedBy").orEmpty()
        val imageRes = intent.getIntExtra("imageRes", 0)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val itemName = name
        tvTitle.text = "Claim $itemName"

        val edtMessage = findViewById<EditText>(R.id.edtMessage)
        val btnSend = findViewById<Button>(R.id.btnSend)

        btnSend.setOnClickListener {
            val message = edtMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                Toast.makeText(this, "Feature in progress...", Toast.LENGTH_SHORT).show()
                // TODO: send email + code gen
            } else {
                Toast.makeText(this, "Please enter a message first", Toast.LENGTH_SHORT).show()
            }
        }

    }
}