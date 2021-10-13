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

    fun storeEffectsSet(effects: List<String>) {
        sp.edit().putString(KEY_EFFECTS, effects.joinToString(separator = ";")).apply()
    }

    fun getEffectsSet(): List<String>? = sp.getString(KEY_EFFECTS, null)?.split(";")
}

private const val KEY_ADDR = "addr"
private const val KEY_EFFECTS = "effects"
