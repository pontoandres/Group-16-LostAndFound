package com.example.lostandfound.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.lostandfound.R
import com.example.lostandfound.data.ItemCache
import com.example.lostandfound.databinding.ActivityItemDetailBinding
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.ui.claimobject.ClaimObjectActivity
import kotlinx.coroutines.launch

/**
 * Item Detail Activity - Displays full information of a selected lost item
 * 
 * Micro-optimizations applied:
 * - Pre-computed strings to avoid repeated concatenation
 * - Single Intent extras extraction
 * - Pre-built Intent for claim button
 * - LifecycleScope instead of MainScope
 * - Method extraction for better code organization
 */
class ItemDetailActivity : AppCompatActivity() {

    // Repo para manejar favoritos y datos locales/remotos
    private val repository by lazy {
        com.example.lostandfound.data.repository.LostItemsRepositoryImpl(this)
    }

    private lateinit var binding: ActivityItemDetailBinding
    
    // Pre-computed strings to avoid repeated concatenation
    private lateinit var postedByText: String
    private lateinit var claimIntent: Intent

    // Estado actual del favorito en esta pantalla
    private var currentFavorite: Boolean = false
    private var currentItemId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Extract all Intent extras once (optimization: single bundle access)
        val extras = intent.extras ?: Bundle()
        val name = extras.getString("name", "")
        val desc = extras.getString("description", "")
        val postedBy = extras.getString("postedBy", "")
        val isOwner = extras.getBoolean("isOwner", true)
        val imageUrl = extras.getString("imageUrl")
        val createdAt = extras.getString("createdAt")
        val itemId = extras.getString("itemId")

        currentItemId = itemId

        // Pre-compute string concatenation once (optimization: avoid repeated string creation)
        postedByText = "Posted by $postedBy"

        // Pre-build Intent for claim button (optimization: reuse instead of recreating)
        claimIntent = Intent(this, ClaimObjectActivity::class.java).apply {
            putExtra("name", name)
            putExtra("description", desc)
            putExtra("postedBy", postedBy)
            putExtra("imageUrl", imageUrl)
            putExtra("itemId", itemId)
        }

        // Bind UI with pre-computed values
        binding.txtItemName.text = name
        binding.txtPostedBy.text = postedByText  // ✅ Uses pre-computed string
        binding.txtDescription.text = desc

        // Setup image loading (extracted method for better organization)
        setupImage(imageUrl)

        // Setup toolbar
        binding.topAppBar.setNavigationOnClickListener { 
            onBackPressedDispatcher.onBackPressed() 
        }

        // Inicializar estado de favorito desde Room (si existe)
        if (itemId != null) {
            lifecycleScope.launch {
                // TODO: Implementar getItemById en LostItemsRepositoryImpl
                val result = repository.getItemById(itemId)
                val item = result.getOrNull()
                currentFavorite = item?.isFavorite ?: false
                updateFavoriteIcon(currentFavorite)
            }
        } else {
            updateFavoriteIcon(false)
        }

        val view = binding.root
        setContentView(view)

        // Toolbar with back
        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // Extras
        val name = intent.getStringExtra("name").orEmpty()
        val desc = intent.getStringExtra("description").orEmpty()
        val postedBy = intent.getStringExtra("postedBy").orEmpty()
        // val imageRes = intent.getIntExtra("imageRes", 0)
        val isOwner = intent.getBooleanExtra("isOwner", true)
        val imageUrl = intent.getStringExtra("imageUrl")
        val createdAt = intent.getStringExtra("createdAt")
        val itemId = intent.getStringExtra("itemId")

        currentItemId = itemId

        // Bind UI
        binding.txtItemName.text = name
        binding.txtPostedBy.text = "Posted by $postedBy"
        binding.txtDescription.text = desc

        val img = binding.imgItem
        if (!imageUrl.isNullOrEmpty()) {
            img.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.ic_broken_image)
            }
        } else {
            img.setImageResource(R.drawable.ic_placeholder)
        }

        // Inicializar estado de favorito desde Room (si existe)
        if (itemId != null) {
            lifecycleScope.launch {
                // TODO: Implementar getItemById en LostItemsRepositoryImpl
                val result = repository.getItemById(itemId)
                val item = result.getOrNull()
                currentFavorite = item?.isFavorite ?: false
                updateFavoriteIcon(currentFavorite)
            }
        } else {
            updateFavoriteIcon(false)
        }

        // Click en el corazón para toggle favorito
        binding.btnFavorite.setOnClickListener {
            val id = currentItemId ?: return@setOnClickListener
            val newState = !currentFavorite

            lifecycleScope.launch {
                // TODO: Implementar toggleFavorite en LostItemsRepositoryImpl
                val result = repository.toggleFavorite(id, newState)

                result.onSuccess {
                    currentFavorite = newState
                    updateFavoriteIcon(newState)
                    Toast.makeText(
                        this@ItemDetailActivity,
                        if (newState) "Added to favorites" else "Removed from favorites",
                        Toast.LENGTH_SHORT
                    ).show()
                }.onFailure { e ->
                    Toast.makeText(
                        this@ItemDetailActivity,
                        "Error updating favorite: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Setup claim button with pre-built Intent
        binding.btnClaim.setOnClickListener {
            startActivity(claimIntent)  // ✅ Reuses pre-built Intent
        }

        // Setup owner section
        binding.ownerSection.visibility = if (isOwner) 
            android.view.View.VISIBLE 
        else 
            android.view.View.GONE

        binding.btnVerify.setOnClickListener {
            val code = binding.edtCode.text?.toString().orEmpty()
            // TODO: validar código (prototipo: solo mostrar toast)
            Toast.makeText(
                this,
                "Feature in progress… Code: $code",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Guardar en cache el último item visto y upsert en Room
        lifecycleScope.launch {
            val item = LostItem(
                id = itemId,
                userId = postedBy,
                title = name,
                description = desc,
                imageUrl = imageUrl,
                createdAt = createdAt ?: java.time.Instant.now().toString()
            )
            ItemCache.saveItem(this@ItemDetailActivity, item)
            repository.upsertLocalFromModel(item)
        }
    }

    /**
     * Extract image loading to separate method
     * Optimization: Better code organization and testability
     */
    private fun setupImage(imageUrl: String?) {
        val img = binding.imgItem
        if (!imageUrl.isNullOrEmpty()) {
            img.load(imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.ic_broken_image)
            }
        } else {
            img.setImageResource(R.drawable.ic_placeholder)

        // Claim
        binding.btnClaim.setOnClickListener {
            val intent = Intent(this, ClaimObjectActivity::class.java).apply {
                putExtra("name", name)
                putExtra("description", desc)
                putExtra("postedBy", postedBy)
                putExtra("imageUrl", imageUrl)
                putExtra("itemId", itemId)
            }
            startActivity(intent)
        }

        // Owner only section
        val ownerSection = binding.ownerSection
        ownerSection.visibility =
            if (isOwner) android.view.View.VISIBLE else android.view.View.GONE

        binding.btnVerify.setOnClickListener {
            val code = binding.edtCode.text?.toString().orEmpty()
            // TODO: validar código (prototipo: solo mostrar toast)
            Toast.makeText(
                this,
                "Feature in progress… Code: $code",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Guardar en cache el último item visto (tu lógica anterior)
        lifecycleScope.launch {
            val item = LostItem(
                id = itemId,
                userId = postedBy,
                title = name,
                description = desc,
                imageUrl = imageUrl,
                createdAt = createdAt ?: java.time.Instant.now().toString()
            )
            ItemCache.saveItem(this@ItemDetailActivity, item)
            repository.upsertLocalFromModel(item)
        }
    }

    /**
     * Actualiza el ícono del botón de favorito según el estado actual
     */
    private fun updateFavoriteIcon(isFavorite: Boolean) {
        val iconRes = if (isFavorite) {
            R.drawable.ic_favorite_filled
        } else {
            R.drawable.ic_favorite_border
        }
        binding.btnFavorite.setImageResource(iconRes)
    }
}
