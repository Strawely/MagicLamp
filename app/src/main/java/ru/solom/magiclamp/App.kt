package ru.solom.magiclamp

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(), ActivityProvider {
    override var activity: AppCompatActivity? = null
}
