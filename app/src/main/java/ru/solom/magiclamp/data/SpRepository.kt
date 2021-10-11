package ru.solom.magiclamp.data

import android.content.Context
import android.content.SharedPreferences
import ru.solom.magiclamp.ActivityProvider
import ru.solom.magiclamp.SP_MAIN
import javax.inject.Inject

class SpRepository @Inject constructor(private val activityProvider: ActivityProvider) {
    private val sp: SharedPreferences
        get() = activityProvider.requireActivity()
            .getSharedPreferences(SP_MAIN, Context.MODE_PRIVATE)

    fun storeAddress(addr: String) {
        sp.edit().putString(KEY_ADDR, addr).apply()
    }

    fun getAddress(): String? {
        return sp.getString(KEY_ADDR, null)
    }
}

private const val KEY_ADDR = "addr"
