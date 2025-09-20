package com.example.lostandfound.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String="",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class LoginViewModel(private val repo: AuthRepository) : ViewModel() {

    private val _ui = MutableStateFlow(LoginUiState())
    val ui: StateFlow<LoginUiState> = _ui

    fun onEmailChanged(v: String) {_ui.value = _ui.value.copy(email = v, error = null)}
    fun onPasswordChanged(v: String) {_ui.value = _ui.value.copy(password = v, error = null)}

    fun login() {
        val email = _ui.value.email.trim()
        val pass = _ui.value.password

        if(email.isEmpty() || pass.length < 8) {
            _ui.value = _ui.value.copy(error = "Enter a valid email and 8+ char password")
            return
        }
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null, success = false)
            val res = repo.login(email, pass)
            _ui.value = res.fold(
                onSuccess = { _ui.value.copy(loading = false, success = true) },
                onFailure = { _ui.value.copy(loading = false, error = it.message ?: "Login failed") }
            )
        }
    }

    // Factory sin DI (simple para el curso)
    class Factory(private val repo: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(repo) as T
        }
    }
}