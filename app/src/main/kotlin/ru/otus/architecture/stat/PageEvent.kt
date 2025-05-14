package ru.otus.architecture.stat

import kotlin.time.Clock
import kotlin.time.Instant

data class PageEvent(
    private val page: String,
    private val action: String,
    private val time: Instant
) : Event {
    override val name: String get() = PAGE

    override val properties: Map<String, String> get() = mapOf(
        NAME to page,
        TIME to time.toString(),
        ACTION to action
    )

    companion object {
        private const val PAGE = "page"
        private const val NAME = "name"
        private const val TIME = "time"
        private const val ACTION = "action"
    }
}

fun <T: Any> T.createPageEvent(
    action: String,
    time: Instant = Clock.System.now()
) = PageEvent(requireNotNull(this::class.simpleName), action, time)