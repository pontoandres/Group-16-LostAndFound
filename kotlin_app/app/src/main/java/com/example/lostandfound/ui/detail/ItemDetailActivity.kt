    package com.example.lostandfound.ui.detail

    import android.content.Intent
    import android.os.Bundle
    import androidx.appcompat.app.AppCompatActivity
    import com.example.lostandfound.R
    import com.example.lostandfound.ui.claimobject.ClaimObjectActivity
    import coil.load
    import android.widget.Toast
    import com.example.lostandfound.databinding.ActivityItemDetailBinding
    import com.example.lostandfound.model.LostItem
    import com.example.lostandfound.data.ItemCache
    import kotlinx.coroutines.MainScope
    import kotlinx.coroutines.launch


    class ItemDetailActivity : AppCompatActivity() {

        private lateinit var binding: ActivityItemDetailBinding

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityItemDetailBinding.inflate(layoutInflater)
            val view = binding.root
            setContentView(view)

            // Toolbar with back
            val toolbar = binding.topAppBar
            toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

            // Extras
            val name = intent.getStringExtra("name").orEmpty()
            val desc = intent.getStringExtra("description").orEmpty()
            val postedBy = intent.getStringExtra("postedBy").orEmpty()
            //val imageRes = intent.getIntExtra("imageRes", 0)
            val isOwner = intent.getBooleanExtra("isOwner", true)
            val imageUrl = intent.getStringExtra("imageUrl")
            val createdAt = intent.getStringExtra("createdAt")
            val itemId = intent.getStringExtra("itemId")

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

            // Claim
            binding.btnClaim
                .setOnClickListener {
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
            ownerSection.visibility = if (isOwner) android.view.View.VISIBLE else android.view.View.GONE

            binding.btnVerify
                .setOnClickListener {
                    val code = binding.edtCode.text?.toString().orEmpty()
                    // TODO: validar código (prototipo: solo mostrar toast)
                    Toast.makeText(this, "Feature in progress… Code: $code", Toast.LENGTH_SHORT).show()
                }

            MainScope().launch {
                val item = LostItem(
                    id = itemId,
                    userId = postedBy,
                    title = name,
                    description = desc,
                    imageUrl = imageUrl,
                    createdAt = createdAt,
                )
                ItemCache.saveItem(this@ItemDetailActivity, item)
            }

        }
    }
