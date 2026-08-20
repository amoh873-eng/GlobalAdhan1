package com.globaladhan.app.di

import android.content.Context
import androidx.room.Room
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.globaladhan.app.data.local.db.GlobalAdhanDatabase
import com.globaladhan.app.data.local.db.dao.BookmarkDao
import com.globaladhan.app.data.local.db.dao.QuranDao
import com.globaladhan.app.data.audio.AudioService
import com.globaladhan.app.data.audio.DemoAudioProvider
import com.globaladhan.app.data.audio.QuranAudioPlayerImpl
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
            // No destructive migration: user data (bookmarks, reading position)
            // must survive updates. Add explicit Migration objects here as the
            // schema evolves (spec §28).
            .addMigrations()
            .build()
    }

    @Provides
    fun provideQuranDao(db: GlobalAdhanDatabase): QuranDao = db.quranDao()

    @Provides
    fun provideBookmarkDao(db: GlobalAdhanDatabase): BookmarkDao = db.bookmarkDao()

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
    fun provideAudioService(player: QuranAudioPlayerImpl): AudioService =
        AudioService(DemoAudioProvider(player))
}
