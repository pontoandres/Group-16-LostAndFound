package com.example.lostandfound.ui.common

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
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

/**
 * Base común:
 * - Mantiene Drawer/Toolbar (setupToolbar()).
 * - Aplica insets del sistema a vistas con id:
 *    R.id.appBar    -> paddingTop
 *    R.id.bottomBar -> paddingBottom
 *    R.id.content   -> paddingLeft/Right
 *
 * Asegúrate de usar esos IDs en tus layouts donde aplique.
 */
abstract class BaseActivity : AppCompatActivity() {

    protected lateinit var drawerLayout: DrawerLayout
    protected lateinit var navigationView: NavigationView
    protected lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        // Edge-to-edge; nosotros ajustamos paddings con insets
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        // Las hijas llaman setContentView(...) y luego setupToolbar() si tienen drawer/topbar
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        applySystemInsets()
    }

    /** Llamar DESPUÉS de setContentView(...) si la pantalla tiene drawer/topbar */
    protected fun setupToolbar() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        toolbar = findViewById(R.id.topAppBar)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener { menuItem ->
            val handled = when (menuItem.itemId) {
                R.id.nav_home -> goIfNot<HomeActivity>()
                R.id.nav_report -> goIfNot<ReportLostItemActivity>(finishCurrent = false)
                R.id.nav_profile -> goIfNot<ProfileActivity>()
                R.id.nav_notifications -> goIfNot<NotificationsActivity>()
                R.id.nav_history -> goIfNot<HistoryActivity>()
                R.id.nav_settings -> goIfNot<SettingsActivity>()
                else -> false
            }
            drawerLayout.closeDrawers()
            handled
        }
    }

    // ===== Insets para TODA la app =====
    private fun applySystemInsets() {
        // Top bar
        findViewById<View?>(R.id.appBar)?.let { top ->
            ViewCompat.setOnApplyWindowInsetsListener(top) { v, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(top = bars.top)
                insets
            }
            top.requestApplyInsets()
        }

        // Bottom bar (botonera inferior)
        findViewById<View?>(R.id.bottomBar)?.let { bottom ->
            ViewCompat.setOnApplyWindowInsetsListener(bottom) { v, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(bottom = bars.bottom)
                insets
            }
            bottom.requestApplyInsets()
        }

        // Contenido principal (bordes laterales / gestos)
        findViewById<View?>(R.id.content)?.let { content ->
            ViewCompat.setOnApplyWindowInsetsListener(content) { v, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.updatePadding(left = bars.left, right = bars.right)
                insets
            }
            content.requestApplyInsets()
        }

        // Propagar al árbol
        findViewById<View>(android.R.id.content)?.requestApplyInsets()
    }

    // Navegación segura para evitar recrear la misma activity
    private inline fun <reified T> goIfNot(finishCurrent: Boolean = true): Boolean {
        if (this !is T) {
            startActivity(Intent(this, T::class.java))
            if (finishCurrent) finish()
        }
        return true
    }
}
