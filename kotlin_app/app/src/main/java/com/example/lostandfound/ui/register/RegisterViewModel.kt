package com.example.lostandfound.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val uniId: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class RegisterViewModel(private val repo: AuthRepository): ViewModel() {
    private val _ui = MutableStateFlow(RegisterUiState())
    val ui: StateFlow<RegisterUiState> = _ui

    fun onEmail(v: String) { _ui.value = _ui.value.copy(email = v, error = null) }
    fun onPass(v: String)  { _ui.value = _ui.value.copy(password = v, error = null) }
    fun onName(v: String)  { _ui.value = _ui.value.copy(name = v, error = null) }
    fun onUniId(v: String) { _ui.value = _ui.value.copy(uniId = v, error = null) }

    fun register() = viewModelScope.launch {
        val s = _ui.value
        val email = s.email.trim()
        val pass  = s.password
        val name  = s.name.trim()
        val uniId = s.uniId.trim()

        when {
            email.isEmpty() -> {
                _ui.value = s.copy(error = "Ingresa tu correo")
                return@launch
            }
            !email.endsWith("@gmail.com", ignoreCase = true) -> {
                _ui.value = s.copy(error = "El correo debe ser @gmail.com")
                return@launch
            }
            pass.length < 8 -> {
                _ui.value = s.copy(error = "La contraseña debe tener al menos 8 caracteres")
                return@launch
            }
            name.isEmpty() -> {
                _ui.value = s.copy(error = "Ingresa tu nombre")
                return@launch
            }
            uniId.isEmpty() -> {
                _ui.value = s.copy(error = "Ingresa tu ID universitario")
                return@launch
            }
        }

        _ui.value = s.copy(loading = true, error = null)
        val res = repo.register(email, pass, name, uniId)
        _ui.value = res.fold(
            onSuccess = { _ui.value.copy(loading = false, success = true) },
            onFailure = { e -> _ui.value.copy(loading = false, error = e.message ?: "No fue posible crear la cuenta") }
        )
    }

    class Factory(private val repo: AuthRepository): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            RegisterViewModel(repo) as T
    }
}