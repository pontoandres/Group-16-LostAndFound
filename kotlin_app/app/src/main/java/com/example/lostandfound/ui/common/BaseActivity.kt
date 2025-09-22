package com.example.lostandfound.ui.common

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.lostandfound.R
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
                    // TODO: start HomeActivity
                    true
                }
                R.id.nav_report -> {
                    // TODO: start ReportActivity
                    true
                }
                R.id.nav_notifications -> {
                    // TODO: start NotificationsActivity
                    true
                }
                R.id.nav_history -> {
                    // TODO: start HistoryActivity
                    true
                }
                R.id.nav_settings -> {
                    // TODO: start SettingsActivity
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawers()
            }
        }

    }
}
