package com.example.lostandfound.ui.home

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.example.lostandfound.databinding.ActivityHomeBinding
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.data.remote.ConnectivityMonitor
import com.example.lostandfound.data.remote.ConnectionState
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.model.Profile
import com.example.lostandfound.ui.common.BaseActivity
import com.example.lostandfound.utils.ConnectivityReceiver
import com.example.lostandfound.workers.enqueueBqRefreshLast30Days
import com.jakewharton.rxbinding4.widget.textChanges
import io.github.jan.supabase.postgrest.postgrest
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class HomeActivity : BaseActivity() {

    private lateinit var adapter: LostItemAdapter
    private lateinit var binding: ActivityHomeBinding
    private lateinit var connectivityReceiver: ConnectivityReceiver
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
                .putExtra("itemId", item.id)

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

    override fun onStart() {
        super.onStart()
        connectivityReceiver = ConnectivityReceiver()
        val filter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(connectivityReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(connectivityReceiver)
    }

    override fun onDestroy() {
        bag.clear()
        super.onDestroy()
    }

    // ===== TECHNIQUE #2: Enhanced Coroutines with explicit Dispatchers =====
    private fun loadLostItems() {
        lifecycleScope.launch {
            try {
                // Switch to IO dispatcher for network operations (Supabase calls)
                val lostItems = withContext(Dispatchers.IO) {
                    SupabaseProvider.client.postgrest["lost_items"].select().decodeList<LostItem>()
                }
                val profiles = withContext(Dispatchers.IO) {
                    SupabaseProvider.client.postgrest["profiles"].select().decodeList<Profile>()
                }

                // Process data on Default dispatcher (CPU work for mapping)
                val mergedItems = withContext(Dispatchers.Default) {
                    lostItems.map { item ->
                        val user = profiles.find { it.id == item.userId }
                        item.copy(legacyPostedBy = user?.name ?: "Unknown")
                    }
                }
                // Back on Main dispatcher for UI updates
                adapter.submitList(mergedItems)
            } catch (e: Exception) {
                val cached = com.example.lostandfound.data.ItemCache.loadAll(this@HomeActivity)
                // Error handling on Main thread for UI updates
                if (cached.isNotEmpty()) {

                    adapter.submitList(cached)

                    Toast.makeText(applicationContext, "Loaded cached items (offline mode)", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(applicationContext, "No cached items available", Toast.LENGTH_SHORT).show()
                    android.util.Log.e("HomeActivity", "Error loading lost items", e)
                    Toast.makeText(this@HomeActivity, "Error loading items: ${e.message}", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }

            }
        }
    }


}
