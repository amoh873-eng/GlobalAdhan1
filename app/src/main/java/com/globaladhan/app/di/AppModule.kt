package com.globaladhan.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.globaladhan.app.data.local.db.GlobalAdhanDatabase
import com.globaladhan.app.data.local.db.dao.BookmarkDao
import com.globaladhan.app.data.local.db.dao.QuranDao
import com.globaladhan.app.data.audio.AudioService
import com.globaladhan.app.data.audio.DemoAudioProvider
import com.globaladhan.app.data.audio.QuranAudioPlayerImpl
import com.globaladhan.app.data.audio.UserAudioProvider
import com.globaladhan.app.domain.audio.QuranAudioPlayer
import com.globaladhan.app.domain.calendar.IslamicCalendar
import com.globaladhan.app.domain.prayer.PrayerTimeCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): GlobalAdhanDatabase {
        return Room.databaseBuilder(
            context,
            GlobalAdhanDatabase::class.java,
            "global_adhan.db"
        )
            // Explicit migrations preserve user data (spec: no destructive migration).
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
        override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `adhkar` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`category` TEXT NOT NULL, `title` TEXT NOT NULL, `arabicText` TEXT NOT NULL, " +
                    "`transliteration` TEXT, `translation` TEXT, `repetitionCount` INTEGER NOT NULL DEFAULT 1, " +
                    "`reward` TEXT, `reference` TEXT, `isFavorite` INTEGER NOT NULL DEFAULT 0, " +
                    "`isCompleted` INTEGER NOT NULL DEFAULT 0)"
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_adhkar_category` ON `adhkar` (`category`)")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `allah_names` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`number` INTEGER NOT NULL, `arabicName` TEXT NOT NULL, `transliteration` TEXT NOT NULL, " +
                    "`meaning` TEXT NOT NULL, `description` TEXT, `isFavorite` INTEGER NOT NULL DEFAULT 0)"
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_allah_names_number` ON `allah_names` (`number`)")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `sajdah_verses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`surahNumber` INTEGER NOT NULL, `surahName` TEXT NOT NULL, `ayahNumber` INTEGER NOT NULL, " +
                    "`verseText` TEXT NOT NULL, `prostrationType` TEXT NOT NULL, `isProstrated` INTEGER NOT NULL DEFAULT 0)"
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sajdah_verses_surahNumber_ayahNumber` ON `sajdah_verses` (`surahNumber`, `ayahNumber`)")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `quran_completions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`completionDate` INTEGER NOT NULL, `duAaText` TEXT NOT NULL, `notes` TEXT)"
            )
        }
    }

    @Provides
    fun provideQuranDao(db: GlobalAdhanDatabase): QuranDao = db.quranDao()

    @Provides
    fun provideBookmarkDao(db: GlobalAdhanDatabase): BookmarkDao = db.bookmarkDao()

    @Provides
    fun provideAdhkarDao(db: GlobalAdhanDatabase): com.globaladhan.app.data.local.db.dao.AdhkarDao =
        db.adhkarDao()

    @Provides
    fun provideAllahNameDao(db: GlobalAdhanDatabase): com.globaladhan.app.data.local.db.dao.AllahNameDao =
        db.allahNameDao()

    @Provides
    fun provideSajdahDao(db: GlobalAdhanDatabase): com.globaladhan.app.data.local.db.dao.SajdahDao =
        db.sajdahDao()

    @Provides
    fun provideQuranCompletionDao(db: GlobalAdhanDatabase): com.globaladhan.app.data.local.db.dao.QuranCompletionDao =
        db.quranCompletionDao()

    @Provides
    @Singleton
    fun provideFusedLocationClient(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun providePrayerTimeCalculator(): PrayerTimeCalculator = PrayerTimeCalculator()

    @Provides
    @Singleton
    fun provideIslamicCalendar(): IslamicCalendar = IslamicCalendar()

    @Provides
    @Singleton
    fun provideQuranAudioPlayer(@ApplicationContext context: Context): QuranAudioPlayer =
        QuranAudioPlayerImpl(context)

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideAudioService(
        player: QuranAudioPlayerImpl,
        @ApplicationContext context: Context
    ): AudioService = AudioService(
        DemoAudioProvider(player),
        UserAudioProvider(context, player)
    )
}
