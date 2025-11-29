package com.example.lostandfound.ui.myreports

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lostandfound.databinding.ActivityMyReportsBinding
import com.example.lostandfound.ui.common.BaseActivity
import com.example.lostandfound.ui.detail.ItemDetailActivity
import com.example.lostandfound.ui.home.LostItemAdapter
import kotlinx.coroutines.launch

/**
 * My Reports Activity - Shows only items reported by the current user
 * Uses Flow for reactive updates (multithreading strategy #3)
 * Implements eventual connectivity pattern
 */
class MyReportsActivity : BaseActivity() {

    private lateinit var binding: ActivityMyReportsBinding
    private lateinit var adapter: LostItemAdapter
    private val viewModel: MyReportsViewModel by viewModels {
        MyReportsViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        // Set title
        binding.topAppBar.title = "My Reports"

        // Setup RecyclerView
        adapter = LostItemAdapter { item ->
            val intent = Intent(this, ItemDetailActivity::class.java).apply {
                putExtra("name", item.getName())
                putExtra("description", item.description)
                putExtra("postedBy", item.getPostedBy())
                putExtra("imageRes", item.getImageRes())
                putExtra("isOwner", true) // User owns their own reports
                putExtra("imageUrl", item.imageUrl)
                putExtra("createdAt", item.createdAt)
                putExtra("itemId", item.id)
            }
            startActivity(intent)
        }

        binding.rvItems.layoutManager = GridLayoutManager(this, 2)
        binding.rvItems.adapter = adapter

        // Observe user items (Flow - reactive updates)
        lifecycleScope.launch {
            viewModel.userItems.collect { items ->
                adapter.submitList(items)
                updateEmptyState(items.isEmpty())
            }
        }

        // Observe loading state
        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                // Show/hide loading indicator if needed
                if (isLoading && adapter.itemCount == 0) {
                    // Show loading
                } else {
                    // Hide loading
                }
            }
        }

        // Observe error state
        lifecycleScope.launch {
            viewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(this@MyReportsActivity, it, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe offline state
        lifecycleScope.launch {
            viewModel.isOffline.collect { offline ->
                if (offline) {
                    Toast.makeText(
                        this@MyReportsActivity,
                        "You are offline. Showing cached reports.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        // Setup pull-to-refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.txtEmptyState.visibility = android.view.View.VISIBLE
            binding.rvItems.visibility = android.view.View.GONE
        } else {
            binding.txtEmptyState.visibility = android.view.View.GONE
            binding.rvItems.visibility = android.view.View.VISIBLE
        }
    }
}

