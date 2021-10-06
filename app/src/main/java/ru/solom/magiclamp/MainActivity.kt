package ru.solom.magiclamp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction().replace(R.id.container, MainFragment()).commit()
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
