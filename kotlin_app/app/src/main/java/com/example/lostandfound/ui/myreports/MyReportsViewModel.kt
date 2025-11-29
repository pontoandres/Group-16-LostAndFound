package com.example.lostandfound.ui.myreports

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.data.remote.ConnectivityMonitor
import com.example.lostandfound.data.remote.ConnectionState
import com.example.lostandfound.data.repository.LostItemsRepository
import com.example.lostandfound.data.repository.LostItemsRepositoryImpl
import com.example.lostandfound.model.LostItem
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for My Reports screen
 * Uses Flow (multithreading strategy #3) for reactive UI updates
 * Implements eventual connectivity pattern
 */
class MyReportsViewModel(
    private val context: Context
) : ViewModel() {

    private val repository: LostItemsRepository = LostItemsRepositoryImpl(context)
    private val TAG = "MyReportsViewModel"

    // Current user ID
    private val currentUserId: String?
        get() = SupabaseProvider.client.auth.currentUserOrNull()?.id

    // ===== TECHNIQUE #3: KOTLIN FLOW =====
    // Flow provides reactive streams with automatic thread management
    // flowOn(Dispatchers.IO) - data fetching happens on I/O thread
    // stateIn - converts Flow to StateFlow for UI observation
    
    /**
     * Observe user's lost items as Flow (reactive updates)
     * Automatically updates UI when local database changes
     */
    val userItems: StateFlow<List<LostItem>> = flow {
        val userId = currentUserId
        if (userId != null) {
            // Emit items from repository Flow
            repository.observeUserItems(userId).collect { items ->
                emit(items)
            }
        } else {
            emit(emptyList())
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Connectivity state (Eventual Connectivity pattern)
    val connectionState: StateFlow<ConnectionState> = ConnectivityMonitor
        .observe(context)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ConnectionState.Available
        )

    val isOffline: StateFlow<Boolean> = connectionState
        .map { it == ConnectionState.Unavailable }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = false
        )

    init {
        // Load initial data
        loadUserItems()
        
        // Monitor connectivity and refresh when online
        viewModelScope.launch {
            connectionState.collect { state ->
                if (state == ConnectionState.Available) {
                    Log.d(TAG, "Connection restored, refreshing user items")
                    refreshUserItems()
                }
            }
        }
    }

    /**
     * Load user's items (offline-first approach)
     */
    fun loadUserItems() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = currentUserId
                if (userId == null) {
                    _error.value = "No active session"
                    _isLoading.value = false
                    return@launch
                }

                // Try to refresh from remote (eventual connectivity)
                // If offline, Flow will emit cached data from Room
                refreshUserItems()
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user items", e)
                _error.value = "Error loading your reports: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh user items from remote (with eventual connectivity)
     */
    private suspend fun refreshUserItems() {
        val userId = currentUserId ?: return
        
        try {
            repository.refreshUserItems(userId).fold(
                onSuccess = {
                    Log.d(TAG, "User items refreshed successfully")
                    _error.value = null
                },
                onFailure = { error ->
                    Log.w(TAG, "Refresh failed (likely offline): ${error.message}")
                    // Don't set error - Flow will emit cached data
                    // This is the eventual connectivity pattern
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Refresh exception: ${e.message}")
            // Flow will still emit cached data
        }
    }

    /**
     * Manual refresh (pull-to-refresh)
     */
    fun refresh() {
        loadUserItems()
    }
}

