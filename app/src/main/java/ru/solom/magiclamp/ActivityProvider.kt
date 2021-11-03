package ru.solom.magiclamp

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity

interface ActivityProvider {
    val activity: AppCompatActivity?
    fun requireActivity(): AppCompatActivity = activity!!
}
