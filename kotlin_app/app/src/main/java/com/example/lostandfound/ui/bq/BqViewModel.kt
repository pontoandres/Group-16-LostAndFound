// app/src/main/java/com/example/lostandfound/ui/bq/BqViewModel.kt
package com.example.lostandfound.ui.bq

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.local.db.entities.BqCategoryEntity
import com.example.lostandfound.data.remote.ConnectivityMonitor
import com.example.lostandfound.data.remote.ConnectionState
import com.example.lostandfound.data.repository.BqRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class UiRow(val category: String, val total: Long, val sharePct: Int)
sealed class UiState<out T> { object Loading: UiState<Nothing>(); data class Data<T>(val value: T): UiState<T>(); data class Error(val msg: String): UiState<Nothing>() }

class BqViewModel(
    private val repo: BqRepository,
    private val appContext: android.content.Context
) : ViewModel() {

    private val _window = MutableStateFlow(defaultWindow()) // last 30 days
    private val _search = MutableStateFlow("")              // RxBinding actualiza esto
    val connection = ConnectivityMonitor.observe(appContext) // Callbacks → Flow

    // Data Flow (Room → UI) con filtrado por search
    val data: StateFlow<UiState<List<UiRow>>> =
        _window.flatMapLatest { w ->
            repo.observe(w.ws, w.we)
        }.combine(_search.debounce(300L)) { list, query ->   // 👈 usa 300L (millis)
            val q = query.trim().lowercase()
            val rows = list
                .filter { it.category.lowercase().contains(q) }
                .map { toUiRow(it) }
            UiState.Data(rows) as UiState<List<UiRow>>
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    init {
        // refresh inicial
        viewModelScope.launch {
            val w = _window.value
            repo.refreshIfNeeded(w.ws, w.we, w.startIso, w.endIso, limit = 10)
        }
        // cuando vuelve la conexión: refrescar
        viewModelScope.launch {
            connection.collect { state ->
                if (state == ConnectionState.Available) {
                    val w = _window.value
                    repo.refreshIfNeeded(w.ws, w.we, w.startIso, w.endIso, limit = 10)
                }
            }
        }
    }

    fun onSearchChanged(text: String) { _search.value = text }

    fun onRefresh() {
        viewModelScope.launch {
            val w = _window.value
            repo.refreshIfNeeded(w.ws, w.we, w.startIso, w.endIso, limit = 10)
        }
    }

    // Helpers
    data class DateWindow(val ws: Long, val we: Long, val startIso: String, val endIso: String)

    private fun defaultWindow(): DateWindow {
        val now = System.currentTimeMillis()
        val ws  = now - 30L * 24 * 60 * 60 * 1000
        return DateWindow(ws, now, toIso(ws), toIso(now))
    }

    private fun toIso(ms: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(ms))
    }

    private fun toUiRow(e: BqCategoryEntity) =
        UiRow(e.category, e.total, (e.share * 100).toInt().coerceIn(0, 100))
}
