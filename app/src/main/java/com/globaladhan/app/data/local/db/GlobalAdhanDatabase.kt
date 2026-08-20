package com.globaladhan.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.globaladhan.app.data.local.db.dao.BookmarkDao
import com.globaladhan.app.data.local.db.dao.QuranDao
import com.globaladhan.app.data.local.db.entity.BookmarkEntity
import com.globaladhan.app.data.local.db.entity.QuranAyahEntity
import com.globaladhan.app.data.local.db.entity.QuranSurahEntity

@Database(
    entities = [
        QuranSurahEntity::class,
        QuranAyahEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GlobalAdhanDatabase : RoomDatabase() {
    abstract fun quranDao(): QuranDao
    abstract fun bookmarkDao(): BookmarkDao
}
