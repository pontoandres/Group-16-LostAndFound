package com.example.lostandfound.ui.detail

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lostandfound.R
import com.example.lostandfound.ui.claimobject.ClaimObjectActivity
import coil.load
import android.widget.Toast
import com.example.lostandfound.databinding.ActivityItemDetailBinding
import com.example.lostandfound.model.LostItem
import com.example.lostandfound.data.ItemCache
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

    private lateinit var binding: ActivityItemDetailBinding
    
    // Pre-computed strings to avoid repeated concatenation
    private lateinit var postedByText: String
    private lateinit var claimIntent: Intent

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
            Toast.makeText(this, "Feature in progress… Code: $code", Toast.LENGTH_SHORT).show()
        }

        // Use lifecycleScope instead of MainScope (optimization: better lifecycle management)
        // Only launch coroutine if itemId is not null
        if (itemId != null) {
            lifecycleScope.launch {
                val item = LostItem(
                    id = itemId ?: "",
                    userId = postedBy,
                    title = name,
                    description = desc,
                    imageUrl = imageUrl,
                    createdAt = createdAt ?: java.time.Instant.now().toString()
                )
                ItemCache.saveItem(this@ItemDetailActivity, item)
            }
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
        }
    }
}
