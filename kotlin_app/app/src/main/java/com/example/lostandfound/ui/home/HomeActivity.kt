package com.example.lostandfound.ui.home

import LostItem
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.google.android.material.textfield.TextInputEditText
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.model.Profile
import io.github.jan.supabase.postgrest.postgrest
import com.example.lostandfound.ui.common.BaseActivity


class HomeActivity : BaseActivity() {

    private lateinit var adapter: LostItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        setupToolbar()

        val rv = findViewById<RecyclerView>(R.id.rvItems)
        val edt = findViewById<TextInputEditText>(R.id.edtSearch)

        adapter = LostItemAdapter { item ->
                val intent = Intent(this, com.example.lostandfound.ui.detail.ItemDetailActivity::class.java)
                .putExtra("name", item.title)
                .putExtra("description", item.description)
                .putExtra("postedBy", item.postedBy)
                .putExtra("imageUrl", item.image_url)
                .putExtra("isOwner", true) // por ahora visible; cambia a true/false para probar

            startActivity(intent)

        }

        rv.layoutManager = GridLayoutManager(this, 2)
        rv.adapter = adapter

        // Datos de muestra (usa los drawables reales)
        /*val items = listOf(
            LostItem(
                id = "umbrella1",
                name = "Umbrella",
                description = "This object was found in room 606 of the ML building on Monday. Chat with me to coordinate delivery.",
                postedBy = "Martin",
                imageRes = R.drawable.ic_umbrella
            ),
            LostItem(
                id = "laptop1",
                name = "Laptop",
                description = "Black Dell laptop found at the cafeteria.",
                postedBy = "Laura",
                imageRes = R.drawable.ic_laptop
            ),
            LostItem(
                id = "wallet1",
                name = "Wallet",
                description = "Brown leather wallet found in the library.",
                postedBy = "Andres",
                imageRes = R.drawable.ic_wallet
            ),
            LostItem(
                id = "key1",
                name = "Key",
                description = "Single house key found near the main entrance.",
                postedBy = "Sofia",
                imageRes = R.drawable.ic_key
            ),
            LostItem(
                id = "headphones1",
                name = "Headphones",
                description = "Orange headphones left in the gym.",
                postedBy = "Carlos",
                imageRes = R.drawable.ic_headphones
            ),
            LostItem(
                id = "watch1",
                name = "Watch",
                description = "Analog watch found in classroom 203.",
                postedBy = "Paula",
                imageRes = R.drawable.ic_watch
            ),
            LostItem(
                id = "glasses1",
                name = "Glasses",
                description = "Pair of glasses found in the library study area.",
                postedBy = "Juan",
                imageRes = R.drawable.ic_glasses
            ),
            LostItem(
                id = "backpack1",
                name = "Backpack",
                description = "Orange backpack left in the parking lot.",
                postedBy = "Maria",
                imageRes = R.drawable.ic_backpack
            )
        )*/

        loadLostItems()

        edt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filterBy(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        // Si por timing el menú aún no está listo, lo posteamos al loop:
        /*
        toolbar.post {
            val reportItem = toolbar.menu.findItem(R.id.action_report)
            val reportBtn = reportItem.actionView
                ?.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReport)

            reportBtn?.setOnClickListener {
                // TODO: abre tu pantalla de "Report a lost item"
                // startActivity(Intent(this, ReportActivity::class.java))
            }
        }
        */



    }

    private fun loadLostItems() {
        lifecycleScope.launch {
            try {
                val lostItems = SupabaseProvider.client
                    .postgrest["lost_items"]
                    .select().decodeList<LostItem>()

                val profiles = SupabaseProvider.client
                    .postgrest["profiles"]
                    .select()
                    .decodeList<Profile>()

                val mergedItems = lostItems.map { item ->
                    val user = profiles.find { it.id == item.user_id }
                    item.copy(postedBy = user?.name ?: "Unknown")
                }

                adapter.submitList(mergedItems)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
