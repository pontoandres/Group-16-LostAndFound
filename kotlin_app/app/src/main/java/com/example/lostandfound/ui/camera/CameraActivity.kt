package com.example.lostandfound.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.example.lostandfound.databinding.ActivityCameraBinding
import com.example.lostandfound.services.ImageAnalysisService
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var cameraExecutor: ExecutorService? = null
    private var imageCapture: ImageCapture? = null

    private var isFlashOn = false
    private var isFrontCamera = false

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1002
        const val EXTRA_IMAGE_PATH = "image_path"
        const val EXTRA_SUGGESTIONS = "suggestions"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()

        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnFlash.setOnClickListener { toggleFlash() }

        binding.btnSwitchCamera.setOnClickListener { switchCamera() }

        binding.btnCapture.setOnClickListener { capturePhoto() }

        binding.btnGallery.setOnClickListener {
            // Si vas a abrir galería, pedimos SOLO lectura según versión
            openGalleryWithPermission()
        }

        binding.btnSettings.setOnClickListener {
            Toast.makeText(this, "Settings opened", Toast.LENGTH_SHORT).show()
        }
    }

    // ===== Permisos =====

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun galleryReadPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

    private fun openGalleryWithPermission() {
        val perm = galleryReadPermission()
        if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(perm),
                STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                val idx = permissions.indexOf(Manifest.permission.CAMERA)
                val granted = idx >= 0 &&
                        grantResults.getOrNull(idx) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    startCamera()
                } else {
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            STORAGE_PERMISSION_REQUEST_CODE -> {
                val perm = galleryReadPermission()
                val idx = permissions.indexOf(perm)
                val granted = idx >= 0 &&
                        grantResults.getOrNull(idx) == PackageManager.PERMISSION_GRANTED
                if (granted) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // ===== Cámara =====

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: return

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

        val selector = if (isFrontCamera)
            CameraSelector.DEFAULT_FRONT_CAMERA
        else
            CameraSelector.DEFAULT_BACK_CAMERA

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                this,
                selector,
                preview,
                imageCapture
            )
        } catch (exc: Exception) {
            Toast.makeText(this, "Camera failed to start", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleFlash() {
        isFlashOn = !isFlashOn
        binding.btnFlash.setColorFilter(
            if (isFlashOn) ContextCompat.getColor(this, com.example.lostandfound.R.color.brand_button)
            else ContextCompat.getColor(this, android.R.color.white)
        )
        // Nota: si quieres flash real al capturar, añade .setFlashMode() en ImageCapture
    }

    private fun switchCamera() {
        isFrontCamera = !isFrontCamera
        bindCameraUseCases()
    }

    private fun capturePhoto() {
        val ic = imageCapture ?: return

        // Guardar en directorio privado de la app (NO requiere permisos de storage)
        val outputDir = getExternalFilesDir(null) ?: filesDir
        val photoFile = File(
            outputDir,
            "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg"
        )

        val options = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        ic.takePicture(
            options,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    try {
                        val compressedFile = compressAndRotateImage(photoFile)
                        analyzeImageAsync(compressedFile)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@CameraActivity,
                            "Error processing image: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent().apply {
                            putExtra(EXTRA_IMAGE_PATH, photoFile.absolutePath)
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@CameraActivity,
                        "Failed to capture photo: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun compressAndRotateImage(imageFile: File): File {
        val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
        val rotated = rotateImageIfRequired(bitmap, imageFile.absolutePath)

        val outputDir = getExternalFilesDir(null) ?: filesDir
        val compressedFile = File(outputDir, "compressed_${imageFile.name}")

        FileOutputStream(compressedFile).use { out ->
            rotated.compress(Bitmap.CompressFormat.JPEG, 80, out)
            out.flush()
        }

        // Borra el original
        imageFile.delete()

        return compressedFile
    }

    private fun rotateImageIfRequired(bitmap: Bitmap, imagePath: String): Bitmap {
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateImage(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun analyzeImageAsync(imageFile: File) {
        android.util.Log.d("CameraActivity", "Starting image analysis for: ${imageFile.absolutePath}")
        cameraExecutor?.execute {
            try {
                val analysisService = ImageAnalysisService()
                val suggestions = runBlocking { analysisService.analyzeImage(imageFile) }
                analysisService.cleanup()
                
                android.util.Log.d("CameraActivity", "Analysis completed. Found ${suggestions.size} suggestions")
                suggestions.forEach { suggestion ->
                    android.util.Log.d("CameraActivity", "Suggestion: ${suggestion.title} (${suggestion.confidence})")
                }
                
                runOnUiThread {
                    android.util.Log.d("CameraActivity", "Creating intent with ${suggestions.size} suggestions")
                    val intent = Intent().apply {
                        putExtra(EXTRA_IMAGE_PATH, imageFile.absolutePath)
                        putParcelableArrayExtra(EXTRA_SUGGESTIONS, suggestions.toTypedArray())
                    }
                    android.util.Log.d("CameraActivity", "Intent created, setting result and finishing")
                    setResult(RESULT_OK, intent)
                    finish()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    android.util.Log.e("CameraActivity", "Image analysis failed", e)
                    val intent = Intent().apply {
                        putExtra(EXTRA_IMAGE_PATH, imageFile.absolutePath)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
        }
    }

    private fun openGallery() {
        // TODO: implementar tu picker/galería real aquí.
        Toast.makeText(this, "Gallery opened", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }
}
