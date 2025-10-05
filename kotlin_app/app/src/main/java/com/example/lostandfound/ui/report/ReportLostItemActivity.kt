package com.example.lostandfound.ui.report

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.lostandfound.databinding.ActivityReportLostItemBinding
import com.example.lostandfound.services.ItemSuggestion
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
        android.util.Log.d("ReportLostItem", "Camera result received: ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            val imagePath = result.data?.getStringExtra(CameraActivity.EXTRA_IMAGE_PATH)
            val suggestions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableArrayListExtra(CameraActivity.EXTRA_SUGGESTIONS, ItemSuggestion::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableArrayListExtra<ItemSuggestion>(CameraActivity.EXTRA_SUGGESTIONS)
            }

            android.util.Log.d("ReportLostItem", "Image path: $imagePath")
            android.util.Log.d("ReportLostItem", "Suggestions count: ${suggestions?.size ?: 0}")
            android.util.Log.d("ReportLostItem", "Intent data: ${result.data}")
            android.util.Log.d("ReportLostItem", "Intent extras: ${result.data?.extras}")

            if (imagePath != null) {
                val imageFile = File(imagePath)
                viewModel.setImageFile(imageFile)
                loadImagePreview(imageFile)

                // Handle suggestions if available
                suggestions?.let { suggestionList ->
                    android.util.Log.d("ReportLostItem", "Processing ${suggestionList.size} suggestions")
                    suggestionList.forEach { suggestion ->
                        android.util.Log.d("ReportLostItem", "Suggestion: ${suggestion.title} (${suggestion.confidence})")
                    }
                    viewModel.setSuggestions(suggestionList)
                    showSuggestions(suggestionList)
                }
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

            android.util.Log.d("ReportLostItem", "SAVE tapped -> title='$title', category='$category'")

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

    private fun showSuggestions(suggestions: List<ItemSuggestion>) {
        android.util.Log.d("ReportLostItem", "showSuggestions called with ${suggestions.size} suggestions")

        if (suggestions.isNotEmpty()) {
            android.util.Log.d("ReportLostItem", "Showing suggestions UI")
            binding.layoutSuggestions.visibility = View.VISIBLE
            binding.txtSuggestionsTitle.text = "AI Suggestions (${suggestions.size})"

            // Show the first suggestion as primary
            val primarySuggestion = suggestions.first()
            binding.btnPrimarySuggestion.text = "${primarySuggestion.title} (${(primarySuggestion.confidence * 100).toInt()}%)"
            binding.btnPrimarySuggestion.setOnClickListener {
                applySuggestion(primarySuggestion)
            }

            // Show additional suggestions if available
            if (suggestions.size > 1) {
                binding.layoutAdditionalSuggestions.visibility = View.VISIBLE
                binding.btnSuggestion2.text = "${suggestions[1].title} (${(suggestions[1].confidence * 100).toInt()}%)"
                binding.btnSuggestion2.setOnClickListener {
                    applySuggestion(suggestions[1])
                }

                if (suggestions.size > 2) {
                    binding.btnSuggestion3.text = "${suggestions[2].title} (${(suggestions[2].confidence * 100).toInt()}%)"
                    binding.btnSuggestion3.setOnClickListener {
                        applySuggestion(suggestions[2])
                    }
                }
            } else {
                binding.layoutAdditionalSuggestions.visibility = View.GONE
            }

            // Adjust description label position to be below suggestions
            val layoutParams = binding.txtDescriptionLabel.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            layoutParams.topToBottom = binding.layoutSuggestions.id
            layoutParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
            binding.txtDescriptionLabel.layoutParams = layoutParams
        } else {
            android.util.Log.d("ReportLostItem", "No suggestions to show")
            binding.layoutSuggestions.visibility = View.GONE

            // Reset description label position to be below image card
            val layoutParams = binding.txtDescriptionLabel.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
            layoutParams.topToBottom = binding.cardImageUpload.id
            layoutParams.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET
            binding.txtDescriptionLabel.layoutParams = layoutParams
        }
    }

    private fun applySuggestion(suggestion: ItemSuggestion) {
        val (title, description, category) = viewModel.applySuggestion(suggestion)

        binding.etTitle.setText(title)
        binding.etDescription.setText(description ?: "")

        // Set category in spinner
        val categories = viewModel.categories.value ?: emptyList()
        val categoryIndex = categories.indexOf(category)
        if (categoryIndex >= 0) {
            binding.spinnerCategory.setSelection(categoryIndex)
        }

        // Hide suggestions after applying
        binding.layoutSuggestions.visibility = View.GONE
        viewModel.clearSuggestions()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = viewModel.state.value?.lostDate ?: Date()
        binding.txtSelectedDate.text = "Lost on: ${dateFormat.format(date)}"
    }
}
