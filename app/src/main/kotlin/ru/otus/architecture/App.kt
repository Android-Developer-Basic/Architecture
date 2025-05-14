package ru.otus.architecture

import android.app.Application
import ru.otus.architecture.prefs.KeyValueStorage
import ru.otus.architecture.prefs.SharedPreferencesStorage
import ru.otus.architecture.stat.StatService

class App : Application() {
    /**
     * Stat service
     */
    val statService = StatService.Logger()

    /**
     * Preferences storage
     */
    val preferences: KeyValueStorage by lazy {
        SharedPreferencesStorage(this, "prefs")
    }
}