package com.example.lostandfound

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {

    // Se construye la primera vez que alguien lo usa
    val client: SupabaseClient by lazy {
        val url = "https://qpfqpjrpjqjtgerbpjtt.supabase.co"
        val anonKey =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFwZnFwanJwanFqdGdlcmJwanR0Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc4NzkwMTIsImV4cCI6MjA3MzQ1NTAxMn0.u-OZsQWP0AkjHcyamIwLEYZuEcLglRInjoBgkY5bb8Q"

        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = anonKey
        ) {
            install(Auth) {
                // 🔹 Hace que la sesión se guarde y se recargue desde almacenamiento local
                alwaysAutoRefresh = true    // refresca tokens automáticamente
                autoSaveToStorage = true    // guarda sesión en disco
                autoLoadFromStorage = true  // la carga al crear el client
            }
            install(Postgrest)
            install(Storage)
        }
    }

    // Helpers por si quieres usarlos en otros lados
    val session get() = client.auth.currentSessionOrNull()
    val userId get() = session?.user?.id
}
