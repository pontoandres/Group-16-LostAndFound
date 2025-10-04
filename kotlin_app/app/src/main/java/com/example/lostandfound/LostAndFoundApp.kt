package com.example.lostandfound

import android.app.Application

class LostAndFoundApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializa Supabase aquí, pero la lógica está en otro archivo
        SupabaseProvider
    }
}
