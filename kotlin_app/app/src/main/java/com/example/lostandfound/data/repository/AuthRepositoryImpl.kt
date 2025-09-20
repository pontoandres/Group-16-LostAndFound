package com.example.lostandfound.data.repository

import kotlinx.coroutines.delay

class AuthRepositoryImpl : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        delay(800) // simula red

        val e = email.trim()
        require(e.isNotBlank()) {"Email Vacio"}
        require(isUniandesEmail(e)) {"El Correo Debe ser @uniandes.edu.co"}
        require(password.length >= 8) {"La contraseña debe tener al menos 8 caracteres"}

        // Aquí iría la llamada real (cuando se conecte al backend)
        Unit
    }

    private fun isUniandesEmail(email: String): Boolean {
        // Más robusto que solo endsWith, evita falsos positivos tipo "x@uniandes.edu.co.hack"
        val domain = email.substringAfter('@', missingDelimiterValue = "")
        return domain.equals("uniandes.edu.co", ignoreCase = true)
    }

    override suspend fun register(email: String, password: String, name: String, uniId: String): Result<Unit> =
        runCatching {
            delay(1000)
            val e = email.trim()
            require(e.isNotBlank()) { "Email vacío" }
            require(isUniandesEmail(e)) { "El correo debe ser @uniandes.edu.co" }
            require(password.length >= 8) { "Contraseña mínima 8" }
            require(name.isNotBlank()) { "Nombre requerido" }
            require(uniId.isNotBlank()) { "ID universitario requerido" }
            Unit
        }

}