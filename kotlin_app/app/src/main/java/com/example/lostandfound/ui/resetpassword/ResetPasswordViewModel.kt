package com.example.lostandfound.ui.resetpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostandfound.SupabaseProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.parseSessionFromUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ResetState {
    data object Idle : ResetState()
    data object Loading : ResetState()
    data class Success(val msg: String) : ResetState()
    data class Error(val msg: String) : ResetState()
}

class ResetPasswordViewModel : ViewModel() {

    private val _state = MutableStateFlow<ResetState>(ResetState.Idle)
    val state: StateFlow<ResetState> = _state

    /** Importa la sesión que viene en el deeplink (URL) */
    fun importSessionFromUrl(url: String) {
        viewModelScope.launch {
            runCatching {
                val session = SupabaseProvider.client.auth.parseSessionFromUrl(url)
                if (session != null) {
                    SupabaseProvider.client.auth.importSession(session)
                } else {
                    error("Enlace inválido o expirado")
                }
            }.onFailure {
                _state.value = ResetState.Error("No se pudo procesar el enlace de restablecimiento")
            }
        }
    }

    /** Cambia la contraseña ya con sesión importada */
    fun updatePassword(newPassword: String) {
        if (newPassword.length < 8) {
            _state.value = ResetState.Error("La contraseña debe tener al menos 8 caracteres")
            return
        }
        _state.value = ResetState.Loading
        viewModelScope.launch {
            runCatching {
                SupabaseProvider.client.auth.updateUser { password = newPassword }
            }.onSuccess {
                _state.value = ResetState.Success("Contraseña actualizada")
            }.onFailure {
                _state.value = ResetState.Error("No se pudo actualizar la contraseña")
            }
        }
    }
}