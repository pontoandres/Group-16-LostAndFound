package com.example.lostandfound.ui.report

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfound.databinding.ActivityReportLostItemBinding
import com.example.lostandfound.ui.camera.CameraActivity

class ReportLostItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportLostItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportLostItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnUpload.setOnClickListener {
            // Open camera page
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        binding.btnExit.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            val description = binding.etDescription.text.toString().trim()
            val details = binding.etDetails.text.toString().trim()
            
            if (description.isNotEmpty() && details.isNotEmpty()) {
                // TODO: Implement actual save logic
                Toast.makeText(this, "Lost item reported successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
