package com.globaladhan.app

import android.app.Application
import com.globaladhan.app.data.local.DataSeeder
import com.globaladhan.app.data.notifications.AlarmRescheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class GlobalAdhanApplication : Application() {

    @Inject lateinit var alarmRescheduler: AlarmRescheduler
    @Inject lateinit var dataSeeder: DataSeeder

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Schedule prayer alarms on every app start (in addition to boot).
        alarmRescheduler.rescheduleAll()
        // Seed Islamic data (Adhkar, 99 Names, Sajdah) on first launch only.
        appScope.launch { dataSeeder.seedIfNeeded() }
    }
}
