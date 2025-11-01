package com.example.lostandfound.ui.claimobject

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.lostandfound.R
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.data.local.db.AppDatabase
import com.example.lostandfound.data.local.db.dao.PendingClaimDao
import com.example.lostandfound.data.local.db.entities.PendingClaim
import com.example.lostandfound.databinding.ActivityClaimObjectBinding
import com.example.lostandfound.ui.common.BaseActivity
import com.example.lostandfound.utils.isOnline
import com.google.android.material.appbar.MaterialToolbar
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClaimObjectActivity : BaseActivity() {

    private lateinit var binding: ActivityClaimObjectBinding
    private lateinit var pendingClaimDao: PendingClaimDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClaimObjectBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val db = AppDatabase.getDatabase(this)
        pendingClaimDao = db.pendingClaimDao()
        Log.d("ClaimDebug", "Database initialized: $pendingClaimDao")

        val toolbar = binding.topAppBar
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val name = intent.getStringExtra("name").orEmpty()
        val description = intent.getStringExtra("description").orEmpty()
        val postedBy = intent.getStringExtra("postedBy").orEmpty()
        val imageRes = intent.getIntExtra("imageRes", 0)

        val tvTitle = binding.tvTitle
        val itemName = name
        tvTitle.text = "Claim $itemName"

        val edtMessage = binding.edtMessage
        val btnSend = binding.btnSend



        val userId = SupabaseProvider.client.auth.currentSessionOrNull()?.user?.id

        btnSend.setOnClickListener {
            val message = edtMessage.text.toString().trim()

            val itemId = intent.getStringExtra("itemId")?: return@setOnClickListener
            Log.d("ClaimDebug", "Received itemId: $itemId")

            if (userId == null) {
                Toast.makeText(this, "No logged in user. Please log in again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (message.isNotEmpty()) {

                if (!isOnline()) {


                    Log.d("ClaimDebug", "User ID: $userId, Item ID: $itemId, Message: $message")

                    lifecycleScope.launch {
                        pendingClaimDao.insertClaim(PendingClaim(userId = userId, itemId = itemId, message = message, code = generateClaimCode(), status = "PENDING"))
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@ClaimObjectActivity, "Claim saved locally. Once you reestablish connection it will be uploaded automatically", Toast.LENGTH_LONG).show()
                        }
                        Log.d("ClaimDebug", "Inserted Claim: User ID: $userId, Item ID: $itemId, Message: $message")
                    }
                } else {
                    Toast.makeText(this, "Feature in progress...", Toast.LENGTH_SHORT).show()
                    val code = generateClaimCode()
                    // TODO: send email + code gen
                }
            } else {
                Toast.makeText(this, "Please enter a message first", Toast.LENGTH_SHORT).show()
            }
        }

    }
    fun generateClaimCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }

}