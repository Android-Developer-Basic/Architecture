package ru.otus.architecture.stat

/**
 * Event
 */
interface Event {
    val name: String
    val properties: Map<String, String>
}