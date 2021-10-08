package ru.solom.magiclamp

@Suppress("MagicNumber")
data class EffectDto(
    val id: Int,
    val name: String,
    val minSpeed: Int,
    val maxSpeed: Int,
    val minScale: Int,
    val maxScale: Int
) {
    companion object {
        fun fromString(string: String): EffectDto {
            val splitString = string.split(',')
            return EffectDto(
                id = splitString[0].substringBefore('.').toInt(),
                name = splitString[0].substringAfter(". "),
                minSpeed = splitString[1].toInt(),
                maxSpeed = splitString[2].toInt(),
                minScale = splitString[3].toInt(),
                maxScale = splitString[4].toInt()
            )
        }
    }
}
