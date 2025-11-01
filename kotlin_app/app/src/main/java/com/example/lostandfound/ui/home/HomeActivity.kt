package com.example.lostandfound.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.databinding.ActivityHomeBinding
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.model.Profile
import com.example.lostandfound.ui.common.BaseActivity
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

import com.example.lostandfound.data.remote.ConnectivityMonitor
import com.example.lostandfound.data.remote.ConnectionState
import com.example.lostandfound.workers.enqueueBqRefreshLast30Days
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch as ktxLaunch
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class HomeActivity : BaseActivity() {

    private lateinit var adapter: LostItemAdapter
    private lateinit var binding: ActivityHomeBinding
    private val bag = CompositeDisposable() // Rx

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        adapter = LostItemAdapter { item ->
            val intent = Intent(
                this,
                com.example.lostandfound.ui.detail.ItemDetailActivity::class.java
            )
                .putExtra("name", item.getName())
                .putExtra("description", item.description)
                .putExtra("postedBy", item.getPostedBy())
                .putExtra("imageRes", item.getImageRes())
                .putExtra("isOwner", false)
                .putExtra("imageUrl", item.imageUrl)
                .putExtra("createdAt", item.createdAt)
            startActivity(intent)
        }

        binding.rvItems.layoutManager = GridLayoutManager(this, 2)
        binding.rvItems.adapter = adapter

        // -- RxJava debounce en búsqueda (cumple RxKotlin)
        bag.add(
            binding.edtSearch.textChanges()
                .debounce(300, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { seq -> adapter.filterBy(seq?.toString().orEmpty()) }
        )

        // Cargar items como ya tenías
        loadLostItems()

        // 1) Encolar la BQ (background) al abrir Home
        enqueueBqRefreshLast30Days(applicationContext)

        // 2) Re-encolar cuando vuelva la conectividad (Callbacks → Flow)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                ConnectivityMonitor.observe(applicationContext).collectLatest { state ->
                    if (state == ConnectionState.Available) {
                        enqueueBqRefreshLast30Days(applicationContext)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        bag.clear()
        super.onDestroy()
    }

    private fun loadLostItems() {
        lifecycleScope.launch {
            try {
                val lostItems = SupabaseProvider.client
                    .postgrest["lost_items"].select()
                    .decodeList<LostItem>()

                val profiles = SupabaseProvider.client
                    .postgrest["profiles"].select()
                    .decodeList<Profile>()

                val mergedItems = lostItems.map { item ->
                    val user = profiles.find { it.id == item.userId }
                    item.copy(legacyPostedBy = user?.name ?: "Unknown")
                }

                adapter.submitList(mergedItems)

            } catch (e: Exception) {
                val cached = com.example.lostandfound.data.ItemCache.loadAll(this@HomeActivity)
                if (cached.isNotEmpty()) {
                    adapter.submitList(cached)
                    Toast.makeText(applicationContext, "Loaded cached items (offline mode)", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "No cached items available", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


}
