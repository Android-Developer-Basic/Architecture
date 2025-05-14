package ru.otus.architecture

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.otus.architecture.prefs.KeyValueStorage

class SettingsViewModel(private val storage: KeyValueStorage) : ViewModel() {
    val prefix: LiveData<String?> = storage.liveData(PREFIX_KEY)
    fun updatePrefix(value: String?) {
        storage[PREFIX_KEY] = value
    }

    class Factory(private val storage: KeyValueStorage) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(storage) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}