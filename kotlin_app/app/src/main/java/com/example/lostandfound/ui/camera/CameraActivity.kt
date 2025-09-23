package com.example.lostandfound.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lostandfound.databinding.ActivityCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityCameraBinding
    
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var cameraExecutor: ExecutorService? = null
    
    private var isFlashOn = false
    private var isFrontCamera = false
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupClickListeners()
        
        if (checkCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }
    
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnFlash.setOnClickListener {
            toggleFlash()
        }
        
        binding.btnSwitchCamera.setOnClickListener {
            switchCamera()
        }
        
        binding.btnCapture.setOnClickListener {
            capturePhoto()
        }
        
        binding.btnGallery.setOnClickListener {
            // Open gallery
            Toast.makeText(this, "Gallery opened", Toast.LENGTH_SHORT).show()
        }
        
        binding.btnSettings.setOnClickListener {
            // Open settings
            Toast.makeText(this, "Settings opened", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
        
        cameraExecutor = Executors.newSingleThreadExecutor()
    }
    
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: return
        
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }
        
        val cameraSelector = if (isFrontCamera) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        
        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview
            )
        } catch (exc: Exception) {
            Toast.makeText(this, "Camera failed to start", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun toggleFlash() {
        isFlashOn = !isFlashOn
        // Update flash button appearance
        binding.btnFlash.setColorFilter(
            if (isFlashOn) ContextCompat.getColor(this, com.example.lostandfound.R.color.brand_button) 
            else ContextCompat.getColor(this, android.R.color.white)
        )
    }
    
    private fun switchCamera() {
        isFrontCamera = !isFrontCamera
        bindCameraUseCases()
    }
    
    private fun capturePhoto() {
        Toast.makeText(this, "Photo captured!", Toast.LENGTH_SHORT).show()
        // Here you would implement actual photo capture logic
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }
}
