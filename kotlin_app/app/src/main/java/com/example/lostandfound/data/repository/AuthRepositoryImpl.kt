package com.example.lostandfound.data.repository

import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.*
import kotlinx.serialization.json.Json

// Json tolerante a claves extra (p.ej. avatar_url) para evitar errores de decodificación
private val json = Json { ignoreUnknownKeys = true }

class AuthRepositoryImpl : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val e = email.trim()
        require(e.isNotBlank()) { "Email vacío" }
        require(isGmailEmail(e)) { "El correo debe ser @gmail.com" }
        require(password.length >= 8) { "La contraseña debe tener al menos 8 caracteres" }

        val supabase = SupabaseProvider.client

        // Sign-In
        supabase.auth.signInWith(Email) {
            this.email = e
            this.password = password
        }

        // Verificar profile
        val user = supabase.auth.currentUserOrNull() ?: error("No se pudo obtener el usuario")

        val res = supabase.from("profiles").select {
            filter { eq("id", user.id) }
        }

        val profile: Profile? = json.decodeFromString<List<Profile>>(res.data).firstOrNull()

        if (profile == null) {
            supabase.auth.signOut()
            error("Tu cuenta no tiene perfil creado. Contacta al soporte.")
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String,
        uniId: String
    ): Result<Unit> = runCatching {
        val e = email.trim()
        require(e.isNotBlank()) { "Email vacío" }
        require(isGmailEmail(e)) { "El correo debe ser @gmail.com" }
        require(password.length >= 8) { "Contraseña mínima 8" }
        require(name.isNotBlank()) { "Nombre requerido" }
        require(uniId.isNotBlank()) { "ID universitario requerido" }

        val supabase = SupabaseProvider.client

        // Sign-Up
        supabase.auth.signUpWith(Email) {
            this.email = e
            this.password = password
        }

        // Upsert profile
        val user = supabase.auth.currentUserOrNull() ?: error("No se pudo obtener el usuario")
        val profile = Profile(id = user.id, name = name, universityId = uniId)
        supabase.from("profiles").upsert(profile)

        // (Opcional) Verificar creado
        val verifyRes = supabase.from("profiles").select {
            filter { eq("id", user.id) }
        }
        val created: Profile = json.decodeFromString<List<Profile>>(verifyRes.data).first()
    }

    override suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        val e = email.trim()
        require(e.isNotBlank()) { "Email vacío" }
        require(isGmailEmail(e)) { "El correo debe ser @gmail.com" }

        val supabase = SupabaseProvider.client
        val redirect = "com.example.lostandfound://login-callback"
        supabase.auth.resetPasswordForEmail(email = e, redirectUrl = redirect)
    }

    override suspend fun updatePassword(newPassword: String): Result<Unit> = runCatching {
        require(newPassword.length >= 8) { "La contraseña debe tener al menos 8 caracteres" }
        val supabase = SupabaseProvider.client
        supabase.auth.updateUser { password = newPassword }
    }
}

// ahora validamos @gmail.com
private fun isGmailEmail(email: String) =
    email.endsWith("@gmail.com", ignoreCase = true)
