package ru.otus.architecture.stat

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 * Helper to log page events
 */
class PageEventHelper(private val statService: StatService): DefaultLifecycleObserver {
    private fun logEvent(owner: LifecycleOwner, action: String) {
        statService.logEvent(
            createPageEvent(
                name = owner::class.simpleName ?: "Unknown",
                action = action
            )
        )
    }

    override fun onCreate(owner: LifecycleOwner) {
        logEvent(owner, "onCreate")
    }

    override fun onStart(owner: LifecycleOwner) {
        logEvent(owner, "onStart")
    }

    override fun onResume(owner: LifecycleOwner) {
        logEvent(owner,"onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        logEvent(owner, "onPause")
    }

    override fun onStop(owner: LifecycleOwner) {
        logEvent(owner, "onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        logEvent(owner, "onDestroy")
    }
}