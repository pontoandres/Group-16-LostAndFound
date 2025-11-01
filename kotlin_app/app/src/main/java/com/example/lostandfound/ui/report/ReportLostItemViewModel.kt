package com.example.lostandfound.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.repository.LostItemsRepository
import com.example.lostandfound.data.repository.LostItemsRepositoryImpl
import com.example.lostandfound.services.ItemSuggestion
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class ReportLostItemState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val imageFile: File? = null,
    val lostDate: Date? = null,
    val suggestions: List<ItemSuggestion> = emptyList(),
    val selectedSuggestion: ItemSuggestion? = null
)

class ReportLostItemViewModel : ViewModel() {
    
    private val repository: LostItemsRepository = LostItemsRepositoryImpl()
    
    private val _state = MutableLiveData(ReportLostItemState())
    val state: LiveData<ReportLostItemState> = _state
    
    private val _categories = MutableLiveData(listOf(
        "Electronics",
        "Clothing", 
        "Accessories",
        "Books",
        "Documents",
        "Keys",
        "Other"
    ))
    val categories: LiveData<List<String>> = _categories
    
    fun setImageFile(file: File) {
        _state.value = _state.value?.copy(imageFile = file)
    }
    
    fun setLostDate(date: Date) {
        _state.value = _state.value?.copy(lostDate = date)
    }
    
    fun clearError() {
        _state.value = _state.value?.copy(error = null)
    }

    fun setSuggestions(suggestions: List<ItemSuggestion>) {
        _state.value = _state.value?.copy(suggestions = suggestions)
    }
    
    fun selectSuggestion(suggestion: ItemSuggestion) {
        _state.value = _state.value?.copy(selectedSuggestion = suggestion)
    }
    
    fun clearSuggestions() {
        _state.value = _state.value?.copy(suggestions = emptyList(), selectedSuggestion = null)
    }
    
    fun applySuggestion(suggestion: ItemSuggestion): Triple<String, String?, String?> {
        return Triple(
            suggestion.title,
            suggestion.description,
            suggestion.category
        )
    }

    fun submitLostItem(
        title: String,
        description: String,
        location: String,
        category: String
    ) {
        if (title.trim().isEmpty()) {
            _state.value = _state.value?.copy(error = "Title is required")
            return
        }

        if (description.trim().isEmpty()) {
            _state.value = _state.value?.copy(error = "Description is required")
            return
        }

        // ===== TECHNIQUE #2: Enhanced Coroutines with explicit Dispatchers =====
        // viewModelScope launches on Main dispatcher (UI thread)
        // withContext(Dispatchers.IO) switches to I/O thread for network/database work
        viewModelScope.launch {
            // This runs on Main dispatcher (UI thread)
            _state.value = _state.value?.copy(isLoading = true, error = null)

            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val lostAt = _state.value?.lostDate?.let { dateFormat.format(it) }

                // Switch to IO dispatcher for network/database operations
                val result = withContext(Dispatchers.IO) {
                    repository.createLostItem(
                        title = title.trim(),
                        description = description.trim().takeIf { it.isNotEmpty() },
                        location = location.trim().takeIf { it.isNotEmpty() },
                        category = category.trim().takeIf { it.isNotEmpty() },
                        lostAt = lostAt,
                        imageFile = _state.value?.imageFile
                    )
                }

                // Back on Main dispatcher (UI thread) for state updates
                result.fold(
                    onSuccess = { lostItem ->
                        _state.value = _state.value?.copy(
                            isLoading = false,
                            success = true,
                            error = null
                        )
                    },
                    onFailure = { error ->
                        _state.value = _state.value?.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to create lost item"
                        )
                    }
                )

            } catch (e: Exception) {
                // Still on Main dispatcher for UI updates
                _state.value = _state.value?.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred"
                )
            }
        }
    }

}
