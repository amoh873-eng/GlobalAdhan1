package com.globaladhan.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "quran_surahs", indices = [Index(value = ["number"], unique = true)])
data class QuranSurahEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: Int,
    val name: String,
    val englishName: String,
    val englishNameTranslation: String,
    val revelationType: String,
    val numberOfAyahs: Int
)

@Entity(
    tableName = "quran_ayahs",
    indices = [
        Index(value = ["surahNumber", "numberInSurah"], unique = true),
        Index(value = ["juz"]),
        Index(value = ["page"])
    ]
)
data class QuranAyahEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahNumber: Int,
    val numberInSurah: Int,
    val text: String,
    val juz: Int,
    val hizbQuarter: Int,
    val page: Int
)

@Entity(tableName = "bookmarks", indices = [Index(value = ["surahNumber", "ayahNumber"], unique = true)])
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surahNumber: Int,
    val ayahNumber: Int,
    val createdAt: Long = System.currentTimeMillis()
)
