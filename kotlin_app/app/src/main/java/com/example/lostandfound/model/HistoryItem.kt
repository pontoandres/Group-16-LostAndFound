package com.example.lostandfound.model

data class HistoryItem (
    val title: String,
    val date: String,
    val objectInfo: String,
    val link: String? = null
)