package com.example.lostandfound.ui.profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lostandfound.databinding.ActivityProfileBinding
import com.example.lostandfound.ui.camera.CameraActivity

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        loadProfileData()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            
            if (name.isNotEmpty() && email.isNotEmpty()) {
                // TODO: Implement actual save logic
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imgProfile.setOnClickListener {
            // Open camera to change profile picture
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadProfileData() {
        // TODO: Load actual user data from repository
        binding.etName.setText("John Doe")
        binding.etEmail.setText("john.doe@example.com")
        binding.txtPublishedItems.text = "5 items published"
    }
}
