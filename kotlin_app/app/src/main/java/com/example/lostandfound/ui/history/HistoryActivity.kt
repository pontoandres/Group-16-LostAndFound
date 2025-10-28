package com.example.lostandfound.ui.history

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityHistoryBinding
import com.example.lostandfound.model.HistoryItem
import com.example.lostandfound.ui.common.BaseActivity

class HistoryActivity: BaseActivity() {

    private lateinit var binding: ActivityHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupToolbar()

        val btnBack = binding.btnBack
        btnBack.setOnClickListener {
            val intent = Intent(this, com.example.lostandfound.ui.home.HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
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

        val recycler = binding.rvHistory
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = HistoryAdapter(historyList)

    }
}