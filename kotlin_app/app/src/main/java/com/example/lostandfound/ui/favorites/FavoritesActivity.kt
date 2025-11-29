package com.example.lostandfound.ui.favorites

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lostandfound.databinding.ActivityFavoritesBinding
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.ui.detail.ItemDetailActivity
import com.example.lostandfound.ui.home.LostItemAdapter
import com.example.lostandfound.data.repository.LostItemsRepositoryImpl
import kotlinx.coroutines.launch

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var adapter: LostItemAdapter
    private val repository by lazy { LostItemsRepositoryImpl(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar back
        binding.topAppBar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Adapter reutilizando el mismo LostItemAdapter
        adapter = LostItemAdapter { item -> openDetail(item) }

        binding.rvFavorites.layoutManager = GridLayoutManager(this, 2)
        binding.rvFavorites.adapter = adapter

        // Observa los favoritos en Room (Flow -> UI)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                repository.observeFavoriteItems().collect { items ->
                    updateUi(items)
                }
            }
        }
    }

    private fun updateUi(items: List<LostItem>) {
        if (items.isEmpty()) {
            binding.rvFavorites.visibility = View.GONE
            binding.txtEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvFavorites.visibility = View.VISIBLE
            binding.txtEmptyState.visibility = View.GONE
            adapter.submitList(items)
        }
    }

    private fun openDetail(item: LostItem) {
        val intent = Intent(this, ItemDetailActivity::class.java).apply {
            putExtra("name", item.getName())
            putExtra("description", item.description)
            putExtra("postedBy", item.getPostedBy())
            putExtra("imageRes", item.getImageRes())
            putExtra("isOwner", false)
            putExtra("imageUrl", item.imageUrl)
            putExtra("createdAt", item.createdAt)
            putExtra("itemId", item.id)
        }
        startActivity(intent)
    }
}
