package ru.solom.magiclamp.data

@Suppress("MagicNumber")
data class LampState(
    val currentId: Int = 0,
    val brightness: Int = 1,
    val speed: Int = 1,
    val scale: Int = 1,
    val isOn: Boolean = false,
) {
    companion object {
        fun fromValues(values: List<String>?) = if (values == null) {
            LampState()
        } else {
            LampState(
                currentId = values[1].toInt(),
                brightness = values[2].toInt(),
                speed = values[3].toInt(),
                scale = values[4].toInt(),
                isOn = values[5] == "1",
            )
        }
    }
}
