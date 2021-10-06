package ru.solom.magiclamp

// Название эффекта,min_скорость,max_скорость,min_масштаб,max_масштаб,
// выбор_ли_цвета_это(0-нет,1-да 2-совмещённый)
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
