package com.example.lostandfound.ui.notifications

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.lostandfound.SupabaseProvider
import com.example.lostandfound.SupabaseProvider.userId
import com.example.lostandfound.data.local.db.AppDatabase
import com.example.lostandfound.data.local.db.entities.NotificationEntity
import com.example.lostandfound.data.local.db.entities.PendingNotificationFetch
import com.example.lostandfound.databinding.ActivityNotificationsBinding
import com.example.lostandfound.data.remote.entities.NotificationRemote
import com.example.lostandfound.model.NotificationUi
import com.example.lostandfound.ui.common.BaseActivity
import com.example.lostandfound.utils.isOnline
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class NotificationsActivity : BaseActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()

        binding.swipeRefresh.setOnRefreshListener {
            refreshNotifications()
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(
                this,
                com.example.lostandfound.ui.home.HomeActivity::class.java
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            finish()
        }

        val db = AppDatabase.getDatabase(this)
        val pendingDao = db.pendingNotificationFetchDao()

        binding.rvNotifications.layoutManager = LinearLayoutManager(this)

        if (!isOnline()) {
            // Save pending request to Room
            MainScope().launch {
                pendingDao.insertFetchRequest(PendingNotificationFetch())
            }

            showOfflineBanner()
            return
        }
        MainScope().launch {
            val notifications = fetchNotifications(userId!!)
            cacheNotifications(this@NotificationsActivity, notifications)
        }

        hideOfflineBanner()
    }

    private fun showOfflineBanner() {
        binding.offlineBanner.visibility = View.VISIBLE
        binding.rvNotifications.visibility = View.GONE
    }

    private fun hideOfflineBanner() {
        binding.offlineBanner.visibility = View.GONE
        binding.rvNotifications.visibility = View.VISIBLE
    }

    private fun NotificationRemote.toUi() = NotificationUi(
        id = id,
        title = title ?: "",
        message = message ?: "",
        highlight = highlight,
        isRead = isRead,
        createdAt = createdAt
    )

    private fun NotificationEntity.toUi() = NotificationUi(
        id = id,
        title = title,
        message = message,
        highlight = highlight,
        isRead = isRead,
        createdAt = createdAt
    )

    private suspend fun fetchNotifications(userId: String): List<NotificationRemote> {
        return SupabaseProvider.client.postgrest["notifications"]
            .select() {
                filter {
                    eq("user_id", userId)
                }
                order(column = "id", order = Order.DESCENDING)
            }
            .decodeList<NotificationRemote>()
    }

    private suspend fun cacheNotifications(context: Context, list: List<NotificationRemote>) {
        val db = AppDatabase.getDatabase(context)
        val dao = db.notificationDao()

        val mapped = list.map {
            NotificationEntity(
                id = it.id,
                userId = it.userId,
                title = it.title ?: "",
                message = it.message ?: "",
                highlight = it.highlight,
                isRead = it.isRead,
                createdAt = it.createdAt
            )
        }

        dao.clear()
        dao.insertAll(mapped)
    }


    private suspend fun loadCachedNotifications(context: Context): List<NotificationEntity> {
        return AppDatabase.getDatabase(context).notificationDao().getAll()
    }

    private fun refreshNotifications() {
        val userId = SupabaseProvider.userId ?: run {
            showOfflineBanner()
            binding.swipeRefresh.isRefreshing = false
            return
        }

        lifecycleScope.launch {
            if (!isOnline()) {
                showOfflineBanner()
                val cached = loadCachedNotifications(this@NotificationsActivity)
                binding.rvNotifications.adapter =
                    NotificationsAdapter(cached.map { it.toUi() })
                binding.swipeRefresh.isRefreshing = false
                return@launch
            }

            hideOfflineBanner()

            try {
                val fresh = fetchNotifications(userId)
                cacheNotifications(this@NotificationsActivity, fresh)

                binding.rvNotifications.adapter =
                    NotificationsAdapter(fresh.map { it.toUi() })
            } catch (e: Exception) {
                val cached = loadCachedNotifications(this@NotificationsActivity)
                binding.rvNotifications.adapter =
                    NotificationsAdapter(cached.map { it.toUi() })
            }

            binding.swipeRefresh.isRefreshing = false
        }
    }


}