package com.example.lostandfound.ui.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ForgotResetState {
    data object Idle : ForgotResetState()
    data object Loading : ForgotResetState()
    data class Success(val msg: String) : ForgotResetState()
    data class Error(val msg: String) : ForgotResetState()
}

class ForgotPasswordViewModel(
    private val repo: AuthRepositoryImpl = AuthRepositoryImpl()
) : ViewModel() {

    private val _state = MutableStateFlow<ForgotResetState>(ForgotResetState.Idle)
    val state: StateFlow<ForgotResetState> = _state

    fun sendReset(email: String) {
        if (email.isBlank()) {
            _state.value = ForgotResetState.Error("Ingresa tu correo")
            return
        }
        if (!email.endsWith("@gmail.com", ignoreCase = true)) {
            _state.value = ForgotResetState.Error("El correo debe ser @gmail.com")
            return
        }

        _state.value = ForgotResetState.Loading
        viewModelScope.launch {
            val ok = repo.sendPasswordReset(email).isSuccess
            _state.value = if (ok)
                ForgotResetState.Success("Te enviamos un correo para restablecer la contraseña")
            else
                ForgotResetState.Error("No fue posible enviar el correo")
        }
    }
}