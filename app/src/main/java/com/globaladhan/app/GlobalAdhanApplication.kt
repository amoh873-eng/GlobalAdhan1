package com.globaladhan.app

import android.app.Application
import com.globaladhan.app.data.notifications.AlarmRescheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class GlobalAdhanApplication : Application() {

    @Inject lateinit var alarmRescheduler: AlarmRescheduler

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Schedule prayer alarms on every app start (in addition to boot).
        // The rescheduler only acts if a saved location exists.
        alarmRescheduler.rescheduleAll()
    }
}
