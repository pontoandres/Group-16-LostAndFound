    package com.example.lostandfound.ui.detail

    import android.content.Intent
    import android.os.Bundle
    import androidx.appcompat.app.AppCompatActivity
    import com.example.lostandfound.R
    import com.google.android.material.appbar.MaterialToolbar
    import android.widget.ImageView
    import android.widget.LinearLayout
    import android.widget.TextView
    import com.example.lostandfound.ui.claimobject.ClaimObjectActivity
    import com.google.android.material.textfield.TextInputEditText
    import coil.load
    import android.view.View
    import android.widget.Toast

    class ItemDetailActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_item_detail)

            // Toolbar with back
            val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
            toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

            // Extras
            val name = intent.getStringExtra("name").orEmpty()
            val desc = intent.getStringExtra("description").orEmpty()
            val postedBy = intent.getStringExtra("postedBy").orEmpty()
            //val imageRes = intent.getIntExtra("imageRes", 0)
            val isOwner = intent.getBooleanExtra("isOwner", true)
            val imageUrl = intent.getStringExtra("imageUrl")

            // Bind UI
            findViewById<TextView>(R.id.txtItemName).text = name
            findViewById<TextView>(R.id.txtPostedBy).text = "Posted by $postedBy"
            findViewById<TextView>(R.id.txtDescription).text = desc

            val img = findViewById<ImageView>(R.id.imgItem)
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
            findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClaim)
                .setOnClickListener {
                    val intent = Intent(this, ClaimObjectActivity::class.java).apply {
                        putExtra("name", name)
                        putExtra("description", desc)
                        putExtra("postedBy", postedBy)
                        putExtra("imageUrl", imageUrl)
                    }
                    startActivity(intent)
                }

            // Owner only section
            val ownerSection = findViewById<LinearLayout>(R.id.ownerSection)
            ownerSection.visibility = if (isOwner) android.view.View.VISIBLE else android.view.View.GONE

            findViewById<com.google.android.material.button.MaterialButton>(R.id.btnVerify)
                .setOnClickListener {
                    val code = findViewById<TextInputEditText>(R.id.edtCode).text?.toString().orEmpty()
                    // TODO: validar código (prototipo: solo mostrar toast)
                    Toast.makeText(this, "Feature in progress… Code: $code", Toast.LENGTH_SHORT).show()
                }
        }
    }
