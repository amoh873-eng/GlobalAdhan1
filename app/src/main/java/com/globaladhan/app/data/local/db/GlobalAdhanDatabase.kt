package com.globaladhan.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.globaladhan.app.data.local.db.dao.AdhkarDao
import com.globaladhan.app.data.local.db.dao.AllahNameDao
import com.globaladhan.app.data.local.db.dao.BookmarkDao
import com.globaladhan.app.data.local.db.dao.QuranCompletionDao
import com.globaladhan.app.data.local.db.dao.QuranDao
import com.globaladhan.app.data.local.db.dao.SajdahDao
import com.globaladhan.app.data.local.db.entity.AdhkarEntity
import com.globaladhan.app.data.local.db.entity.AllahNameEntity
import com.globaladhan.app.data.local.db.entity.BookmarkEntity
import com.globaladhan.app.data.local.db.entity.QuranAyahEntity
import com.globaladhan.app.data.local.db.entity.QuranCompletionEntity
import com.globaladhan.app.data.local.db.entity.QuranSurahEntity
import com.globaladhan.app.data.local.db.entity.SajdahEntity

@Database(
    entities = [
        QuranSurahEntity::class,
        QuranAyahEntity::class,
        BookmarkEntity::class,
        AdhkarEntity::class,
        AllahNameEntity::class,
        SajdahEntity::class,
        QuranCompletionEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GlobalAdhanDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun adhkarDao(): AdhkarDao
    abstract fun allahNameDao(): AllahNameDao
    abstract fun sajdahDao(): SajdahDao
    abstract fun quranCompletionDao(): QuranCompletionDao
}
