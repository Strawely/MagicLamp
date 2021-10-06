package ru.solom.magiclamp

import android.util.Log

val ByteArray.lampValues: List<String>
    get() {
        val delimiter = ' '.code
        val builder = StringBuilder()
        val result = mutableListOf<String>()
        forEach { c ->
            val cInt = c.toInt()
            when(cInt) {
                delimiter -> {
                    result.add(builder.toString())
                    builder.clear()
                }
                0 -> {
                    /* no-op */
                }
                else -> builder.append(Char(cInt))
            }
        }
        result.add(builder.toString())
        return result
    }
