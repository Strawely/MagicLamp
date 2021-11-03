package ru.solom.magiclamp.alarms

data class AlarmState(
    val dayNum: Int,
    val isOn: Boolean,
    val time: Time,
)
