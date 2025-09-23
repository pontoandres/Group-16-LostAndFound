package com.example.lostandfound.ui.common

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.lostandfound.R
import com.example.lostandfound.ui.history.HistoryActivity
import com.example.lostandfound.ui.home.HomeActivity
import com.example.lostandfound.ui.notifications.NotificationsActivity
import com.example.lostandfound.ui.profile.ProfileActivity
import com.example.lostandfound.ui.report.ReportLostItemActivity
import com.example.lostandfound.ui.settings.SettingsActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navigationView: NavigationView
    protected lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Notice: child Activities will call setContentView(),
        // and after that they must call setupToolbar()

    }

    protected fun setupToolbar() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.topAppBar)

        // Attach the toolbar as the ActionBar
        setSupportActionBar(toolbar)

        // Create the toggle that shows the burger/arrow animation
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState() // keep icon in sync

        // Drawer item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    if (this !is HomeActivity) {
                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                    true
                }
                R.id.nav_report -> {
                    if (this !is ReportLostItemActivity) {
                        startActivity(Intent(this, ReportLostItemActivity::class.java))
                    }
                    true
                }
                R.id.nav_profile -> {
                    if (this !is ProfileActivity) {
                        startActivity(Intent(this, ProfileActivity::class.java))
                    }
                    true
                }
                R.id.nav_notifications -> {
                    if (this !is NotificationsActivity) {
                        startActivity(Intent(this, NotificationsActivity::class.java))
                    }
                    true
                }
                R.id.nav_history -> {
                    if (this !is HistoryActivity) {
                        startActivity(Intent(this, HistoryActivity::class.java))
                    }
                    true
                }
                R.id.nav_settings -> {
                    if (this !is SettingsActivity) {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawers()
            }
        }

    }
}
