package com.globaladhan.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuranSurah(
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val numberOfAyahs: Int
)

@Serializable
data class QuranAyah(
    val number: Int,
    val surahNumber: Int,
    val text: String,
    val numberInSurah: Int,
    val juz: Int,
    val hizbQuarter: Int,
    val page: Int
) {
    /**
     * Split the ayah into Arabic words for word-by-word highlighting.
     * Uses Unicode whitespace as the word boundary, which is correct for the
     * Tanzil Uthmani text (words are space-separated; diacritics attach to
     * their word).
     */
    val words: List<String>
        get() = text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
}

@Serializable
data class JuzInfo(
    val juz: Int,
    val startSurah: Int,
    val startAyah: Int,
    val endSurah: Int,
    val endAyah: Int
)
