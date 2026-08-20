package com.globaladhan.app.data.repository

import android.content.Context
import com.globaladhan.app.data.local.db.dao.BookmarkDao
import com.globaladhan.app.data.local.db.dao.QuranDao
import com.globaladhan.app.data.local.db.entity.BookmarkEntity
import com.globaladhan.app.data.local.db.entity.QuranAyahEntity
import com.globaladhan.app.data.local.db.entity.QuranSurahEntity
import com.globaladhan.app.domain.model.QuranAyah
import com.globaladhan.app.domain.model.QuranSurah
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuranRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quranDao: QuranDao,
    private val bookmarkDao: BookmarkDao
) {

    /** Whether the full Quran text has been loaded into the DB. */
    val isLoaded: Boolean
        get() = runBlockingCount() > 0

    private fun runBlockingCount(): Int {
        return kotlinx.coroutines.runBlocking { quranDao.countAyahs() }
    }

    val surahs: Flow<List<QuranSurah>> = quranDao.observeSurahs().map { entities ->
        entities.map { it.toDomain() }
    }

    suspend fun getSurah(number: Int): QuranSurah? = quranDao.getSurah(number)?.toDomain()

    suspend fun getAyahs(surahNumber: Int): List<QuranAyah> =
        quranDao.getAyahs(surahNumber).map { it.toDomain() }

    suspend fun getAyahsByJuz(juz: Int): List<QuranAyah> =
        quranDao.getAyahsByJuz(juz).map { it.toDomain() }

    suspend fun getAyahsByPage(page: Int): List<QuranAyah> =
        quranDao.getAyahsByPage(page).map { it.toDomain() }

    suspend fun search(query: String): List<QuranAyah> =
        quranDao.search(query).map { it.toDomain() }

    suspend fun isBookmarked(surah: Int, ayah: Int): Boolean =
        bookmarkDao.isBookmarked(surah, ayah) > 0

    fun observeBookmarks(): Flow<List<Pair<Int, Int>>> =
        bookmarkDao.observeAll().map { list -> list.map { it.surahNumber to it.ayahNumber } }

    suspend fun addBookmark(surah: Int, ayah: Int) {
        bookmarkDao.insert(BookmarkEntity(surahNumber = surah, ayahNumber = ayah))
    }

    suspend fun removeBookmark(surah: Int, ayah: Int) {
        bookmarkDao.findByAyah(surah, ayah)?.let { bookmarkDao.delete(it) }
    }

    /**
     * Import the Quran from a bundled JSON asset.
     * Expected format (from Tanzil / Quran.com-compatible export):
     * {
     *   "surahs": [ { "number": 1, "name": "...", "englishName": "...",
     *                 "englishNameTranslation": "...", "revelationType": "...",
     *                 "ayahs": [ { "number": 1, "text": "...", "juz": 1, "hizbQuarter": 1, "page": 1 } ] } ]
     * }
     */
    suspend fun importFromAsset(assetName: String = "quran.json"): Boolean {
        return withContext(Dispatchers.IO) {
            runCatching {
                val json = context.assets.open(assetName).bufferedReader().use { it.readText() }
                val root = JSONObject(json)
                val surahsArray = root.getJSONArray("surahs")

                val surahEntities = mutableListOf<QuranSurahEntity>()
                val ayahEntities = mutableListOf<QuranAyahEntity>()

                for (i in 0 until surahsArray.length()) {
                    val surah = surahsArray.getJSONObject(i)
                    val number = surah.getInt("number")
                    surahEntities += QuranSurahEntity(
                        number = number,
                        name = surah.getString("name"),
                        englishName = surah.getString("englishName"),
                        englishNameTranslation = surah.optString("englishNameTranslation"),
                        revelationType = surah.optString("revelationType", "Meccan"),
                        numberOfAyahs = surah.getJSONArray("ayahs").length()
                    )
                    val ayahs = surah.getJSONArray("ayahs")
                    for (j in 0 until ayahs.length()) {
                        val ayah = ayahs.getJSONObject(j)
                        ayahEntities += QuranAyahEntity(
                            surahNumber = number,
                            numberInSurah = ayah.getInt("number"),
                            text = ayah.getString("text"),
                            juz = ayah.optInt("juz", 1),
                            hizbQuarter = ayah.optInt("hizbQuarter", 1),
                            page = ayah.optInt("page", 1)
                        )
                    }
                }

                quranDao.insertSurahs(surahEntities)
                quranDao.insertAyahs(ayahEntities)
                true
            }.getOrDefault(false)
        }
    }

    private fun QuranSurahEntity.toDomain() = QuranSurah(
        number = number,
        name = name,
        englishName = englishName,
        englishNameTranslation = englishNameTranslation,
        revelationType = revelationType,
        numberOfAyahs = numberOfAyahs
    )

    private fun QuranAyahEntity.toDomain() = QuranAyah(
        number = numberInSurah,
        surahNumber = surahNumber,
        text = text,
        numberInSurah = numberInSurah,
        juz = juz,
        hizbQuarter = hizbQuarter,
        page = page
    )
}
