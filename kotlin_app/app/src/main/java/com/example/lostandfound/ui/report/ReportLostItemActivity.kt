package com.example.lostandfound.ui.report

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.lostandfound.databinding.ActivityReportLostItemBinding
import com.example.lostandfound.ui.camera.CameraActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ReportLostItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportLostItemBinding
    private val viewModel: ReportLostItemViewModel by viewModels()
    
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val imagePath = result.data?.getStringExtra("image_path")
            if (imagePath != null) {
                val imageFile = File(imagePath)
                viewModel.setImageFile(imageFile)
                loadImagePreview(imageFile)
            }
        }
    }
    
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // For now, we'll use the camera for both options
            // In a real app, you'd handle gallery selection here
            Toast.makeText(this, "Please use camera for now", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportLostItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup category spinner
        viewModel.categories.observe(this) { categories ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter
        }
        
        // Set default date to today
        viewModel.setLostDate(Date())
        updateDateDisplay()
    }

    private fun setupClickListeners() {
        binding.btnUpload.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            cameraLauncher.launch(intent)
        }

        binding.btnExit.setOnClickListener {
            finish()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem?.toString() ?: ""
            
            viewModel.submitLostItem(title, description, location, category)
        }
        
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }
    }
    
    private fun observeViewModel() {
        viewModel.state.observe(this, Observer { state ->
            binding.progressBar.visibility = if (state.isLoading) 
                android.view.View.VISIBLE else android.view.View.GONE
            
            binding.btnSave.isEnabled = !state.isLoading
            
            state.error?.let { error ->
                Toast.makeText(this, error, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
            
            if (state.success) {
                Toast.makeText(this, "Lost item reported successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }
    
    private fun loadImagePreview(imageFile: File) {
        Glide.with(this)
            .load(imageFile)
            .into(binding.imgPlaceholder)
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        viewModel.state.value?.lostDate?.let { date ->
            calendar.time = date
        }
        
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth)
                }.time
                viewModel.setLostDate(selectedDate)
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }
    
    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = viewModel.state.value?.lostDate ?: Date()
        binding.txtSelectedDate.text = "Lost on: ${dateFormat.format(date)}"
    }
}
