package ru.otus.architecture

import android.app.Application
import ru.otus.architecture.stat.StatService

class App : Application() {
    /**
     * Stat service
     */
    val statService = StatService.Logger()
}