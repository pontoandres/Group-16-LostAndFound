package com.example.lostandfound.ui.recent
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lostandfound.data.ItemCache
import com.example.lostandfound.databinding.ActivityRecentlyViewedBinding
import com.example.lostandfound.ui.common.BaseActivity
import com.example.lostandfound.ui.detail.ItemDetailActivity
import kotlinx.coroutines.launch


class RecentlyViewedActivity : BaseActivity() {
    private lateinit var binding: ActivityRecentlyViewedBinding
    private lateinit var adapter: RecentlyViewedAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecentlyViewedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        adapter = RecentlyViewedAdapter { item ->
            val i = Intent(this, ItemDetailActivity::class.java)

            i.putExtra("name", item.getName())
            i.putExtra("description", item.description)
            i.putExtra("postedBy", item.getPostedBy())
            i.putExtra("imageUrl", item.imageUrl)
            i.putExtra("createdAt", item.createdAt)
            i.putExtra("itemId", item.id)
            startActivity(i)
        }
        binding.rvRecentItems.layoutManager = GridLayoutManager(this, 2)
        binding.rvRecentItems.adapter = adapter

        loadItems()

        binding.swipeRefresh.setOnRefreshListener {
            loadItems()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun loadItems() {
        lifecycleScope.launch {
            val recent = ItemCache.loadRecent(this@RecentlyViewedActivity)
            adapter.submitList(recent)
        }
    }
}