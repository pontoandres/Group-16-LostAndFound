    package com.example.lostandfound.ui.detail

    import android.os.Bundle
    import androidx.appcompat.app.AppCompatActivity
    import com.example.lostandfound.R
    import com.google.android.material.appbar.MaterialToolbar
    import android.widget.ImageView
    import android.widget.LinearLayout
    import android.widget.TextView
    import com.google.android.material.textfield.TextInputEditText

    class ItemDetailActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_item_detail)

            // Toolbar con back
            val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
            toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

            // Extras
            val name = intent.getStringExtra("name").orEmpty()
            val desc = intent.getStringExtra("description").orEmpty()
            val postedBy = intent.getStringExtra("postedBy").orEmpty()
            val imageRes = intent.getIntExtra("imageRes", 0)
            val isOwner = intent.getBooleanExtra("isOwner", true)

            // Bind UI
            findViewById<TextView>(R.id.txtItemName).text = name
            findViewById<TextView>(R.id.txtPostedBy).text = "Posted by $postedBy"
            findViewById<TextView>(R.id.txtDescription).text = desc
            if (imageRes != 0) findViewById<ImageView>(R.id.imgItem).setImageResource(imageRes)

            // Claim
            findViewById<com.google.android.material.button.MaterialButton>(R.id.btnClaim)
                .setOnClickListener {
                    // TODO: flujo de claim (prototipo: mostrar toast o navegar)
                }

            // Owner-only section
            val ownerSection = findViewById<LinearLayout>(R.id.ownerSection)
            ownerSection.visibility = if (isOwner) android.view.View.VISIBLE else android.view.View.GONE

            findViewById<com.google.android.material.button.MaterialButton>(R.id.btnVerify)
                .setOnClickListener {
                    val code = findViewById<TextInputEditText>(R.id.edtCode).text?.toString().orEmpty()
                    // TODO: validar código (prototipo: solo mostrar toast)
                }
        }
    }
