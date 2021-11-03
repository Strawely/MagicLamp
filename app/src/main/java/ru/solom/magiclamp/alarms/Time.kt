package ru.solom.magiclamp.alarms

data class Time(
    val hours: Int,
    val minutes: Int
) {
    val lampFormat get() = hours * MINUTES_IN_HOUR + minutes

    override fun toString() = "$hours:%02d".format(minutes)

    companion object {
        fun parseLampFormat(value: Int) = Time(value / MINUTES_IN_HOUR, value % MINUTES_IN_HOUR)
    }
}

private const val MINUTES_IN_HOUR = 60
