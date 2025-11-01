package com.example.lostandfound.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

class HomeActivity : BaseActivity() {

    private lateinit var adapter: LostItemAdapter
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_home)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        setupToolbar()

        val rv = binding.rvItems
        val edt = binding.edtSearch


        adapter = LostItemAdapter { item ->
            // Usa el modelo de Esteban: getName(), getPostedBy(), getImageRes()
            val intent = Intent(
                this,
                com.example.lostandfound.ui.detail.ItemDetailActivity::class.java
            )
                .putExtra("name", item.getName())
                .putExtra("description", item.description)
                .putExtra("postedBy", item.getPostedBy())
                .putExtra("imageRes", item.getImageRes())
                // si tienes sesión, calcula dueño real: currentUserId == item.userId
                .putExtra("isOwner", false)
                .putExtra("imageUrl", item.imageUrl)
                .putExtra("createdAt", item.createdAt)
                .putExtra("itemId", item.id)

            startActivity(intent)
        }

        rv.layoutManager = GridLayoutManager(this, 2)
        rv.adapter = adapter

        loadLostItems()

        edt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterBy(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadLostItems() {
        lifecycleScope.launch {
            try {
                // Decodifica con el modelo de Esteban
                val lostItems = SupabaseProvider.client
                    .postgrest["lost_items"]
                    .select()
                    .decodeList<LostItem>()

                val profiles = SupabaseProvider.client
                    .postgrest["profiles"]
                    .select()
                    .decodeList<Profile>()

                // En Esteban: el campo es userId (no user_id) y no existe postedBy.
                // Para mostrar el nombre del usuario en UI, guardamos en legacyPostedBy
                // para que getPostedBy() lo tome.
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
