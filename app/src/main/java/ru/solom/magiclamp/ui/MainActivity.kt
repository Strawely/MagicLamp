package ru.solom.magiclamp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import ru.solom.magiclamp.App
import ru.solom.magiclamp.R

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onPostResume() {
        super.onPostResume()
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        findViewById<BottomNavigationView>(R.id.bottom_nav_view)
            .setupWithNavController(navHost.navController)
    }

    override fun onStart() {
        (application as App).activity = this
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            (application as App).activity = null
        }
    }
}
