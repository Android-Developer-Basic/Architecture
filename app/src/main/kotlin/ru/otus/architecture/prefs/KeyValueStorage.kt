package ru.otus.architecture.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData

/**
 * Key-value storage interface
 */
interface KeyValueStorage {
    /**
     * Gets value by key
     */
    operator fun get(key: String): String?

    /**
     * Sets value by key
     */
    operator fun set(key: String, value: String?)

    /**
     * Gets value by key as live data
     */
    fun liveData(key: String): LiveData<String?>

    /**
     * Clears all keys
     */
    fun clear()
}

/**
 * Uses shared preferences as a storage
 */
class SharedPreferencesStorage(context: Context, name: String) : KeyValueStorage {
    private val prefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    override fun get(key: String): String? = prefs.getString(key, null)

    override fun set(key: String, value: String?) {
        prefs.edit().putString(key, value).apply()
    }

    override fun liveData(key: String): LiveData<String?> = object : LiveData<String?>(get(key)) {
        private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, changedKey ->
            if (changedKey == key) {
                postValue(get(key))
            }
        }

        override fun onActive() {
            super.onActive()
            postValue(get(key))
            prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        }

        override fun onInactive() {
            super.onInactive()
            prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        }
    }

    override fun clear() {
        prefs.edit().clear().apply()
    }
}