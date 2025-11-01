package com.example.lostandfound.ui.home

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.model.Profile
import com.example.lostandfound.ui.common.BaseActivity
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class HomeActivity : BaseActivity() {

    private lateinit var adapter: LostItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupToolbar()

        val rv = findViewById<RecyclerView>(R.id.rvItems)
        val edt = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edtSearch)

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
                e.printStackTrace()
            }
        }
    }
}
