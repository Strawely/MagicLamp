package ru.solom.magiclamp.data

@Suppress("MagicNumber")
data class EffectDto(
    val id: Int,
    val name: String,
    val minSpeed: Int,
    val maxSpeed: Int,
    val minScale: Int,
    val maxScale: Int,
    val scaleUnavailable: Boolean,
    val speedUnavailable: Boolean
) {
    override fun toString(): String {
        fun StringBuilder.appendSeparator() = append(",")
        return buildString {
            append("$id. ")
            append(name)
            appendSeparator()
            append(minSpeed)
            appendSeparator()
            append(maxSpeed)
            appendSeparator()
            append(minScale)
            appendSeparator()
            append(maxScale)
            appendSeparator()
        }
    }

    companion object {
        fun fromString(string: String): EffectDto {
            val splitString = string.split(',')
            return EffectDto(
                id = splitString[0].substringBefore('.').toInt(),
                name = splitString[0].substringAfter(". "),
                minSpeed = splitString[1].toInt(),
                maxSpeed = splitString[2].toInt(),
                minScale = splitString[3].toInt(),
                maxScale = splitString[4].toInt(),
                scaleUnavailable = splitString[3] == splitString[4],
                speedUnavailable = splitString[1] == splitString[2]
            )
        }
    }
}
