package ru.solom.magiclamp

import android.app.Activity
import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(), ActivityProvider {
    override var activity: Activity? = null
}