package com.example.lostandfound.ui.history

import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.model.HistoryItem
import com.example.lostandfound.ui.common.BaseActivity

class HistoryActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        setupToolbar()

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val historyList = listOf(
            HistoryItem(
                "Claim Request was verified",
                "12/09/2025 at 1:43 PM",
                "Umbrella found by Martin"
            ),
            HistoryItem(
                "Claim Request sent",
                "10/09/2025 at 5:43 PM",
                "Umbrella found by Martin",
                "link to post"
            ),
            HistoryItem(
                "Found Object posted",
                "23/07/2025 at 5:43 PM",
                "Water bottle found",
                "link to post"
            )
        )

        val recycler = findViewById<RecyclerView>(R.id.rvHistory)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = HistoryAdapter(historyList)

    }
}