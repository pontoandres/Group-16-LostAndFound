package com.example.lostandfound.ui.myreports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyReportsViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyReportsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyReportsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

