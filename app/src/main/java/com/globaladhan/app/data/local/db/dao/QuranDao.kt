package com.globaladhan.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.globaladhan.app.data.local.db.entity.QuranAyahEntity
import com.globaladhan.app.data.local.db.entity.QuranSurahEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    @Query("SELECT * FROM quran_surahs ORDER BY number")
    fun observeSurahs(): Flow<List<QuranSurahEntity>>

    @Query("SELECT * FROM quran_surahs ORDER BY number")
    suspend fun getSurahs(): List<QuranSurahEntity>

    @Query("SELECT * FROM quran_surahs WHERE number = :number")
    suspend fun getSurah(number: Int): QuranSurahEntity?

    @Query("SELECT * FROM quran_ayahs WHERE surahNumber = :surahNumber ORDER BY numberInSurah")
    suspend fun getAyahs(surahNumber: Int): List<QuranAyahEntity>

    @Query("SELECT * FROM quran_ayahs WHERE juz = :juz ORDER BY surahNumber, numberInSurah")
    suspend fun getAyahsByJuz(juz: Int): List<QuranAyahEntity>

    @Query("SELECT * FROM quran_ayahs WHERE page = :page ORDER BY surahNumber, numberInSurah")
    suspend fun getAyahsByPage(page: Int): List<QuranAyahEntity>

    @Query("SELECT * FROM quran_ayahs WHERE text LIKE '%' || :query || '%' LIMIT 200")
    suspend fun search(query: String): List<QuranAyahEntity>

    @Query("SELECT COUNT(*) FROM quran_ayahs")
    suspend fun countAyahs(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurahs(surahs: List<QuranSurahEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAyahs(ayahs: List<QuranAyahEntity>)
}
