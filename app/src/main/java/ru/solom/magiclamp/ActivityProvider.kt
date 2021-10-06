package ru.solom.magiclamp

import android.app.Activity

interface ActivityProvider {
    val activity: Activity?
    fun requireActivity(): Activity = activity!!
}
