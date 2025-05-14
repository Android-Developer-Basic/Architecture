package ru.otus.architecture.stat

import android.util.Log

/**
 * Stat service
 */
interface StatService {

    /**
     * Logs event
     */
    fun logEvent(event: Event)

    /**
     * Android log service implementation
     */
    class Logger: StatService {
        override fun logEvent(event: Event) {
            Log.i(  TAG, "Event: ${event.name}, Properties ${event.properties}")
        }

        companion object {
            private const val TAG = "StatService"
        }
    }
}