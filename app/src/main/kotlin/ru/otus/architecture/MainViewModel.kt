package ru.otus.architecture

import android.graphics.Rect
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.google.mlkit.vision.text.Text
import ru.otus.architecture.prefs.KeyValueStorage

class MainViewModel(private val storage: KeyValueStorage, private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private var detectedText: String = ""

    private val _content: MutableLiveData<String> = savedStateHandle.getLiveData(CONTENT_KEY, "")
    val content: LiveData<String> = MediatorLiveData("").apply {
        addSource(_content) { contentUpdate ->
            value = mergeContent(storage[PREFIX_KEY], contentUpdate)
        }
        addSource(storage.liveData(PREFIX_KEY)) { prefixUpdate ->
            value = mergeContent(prefixUpdate, _content.value)
        }
    }.distinctUntilChanged()

    private fun mergeContent(prefix: String?, content: String?): String {
        val contentPrefix = prefix?.takeIf { it.isNotBlank() }?.let { "$it\n" } ?: ""
        val contentValue = content.orEmpty()
        return contentPrefix + contentValue
    }

    val shareEnabled: LiveData<Boolean> = MediatorLiveData(false).apply {
        addSource(content) { value = it.isNotBlank() }
    }

    private val _addEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    val addEnabled: LiveData<Boolean> get() = _addEnabled

    private val _detectedRects: MutableLiveData<List<Rect>> = MutableLiveData(emptyList())
    val detectedRects: LiveData<List<Rect>> get() = _detectedRects

    fun updateRecognizedText(text: Text) {
        // Update detected text and enable add button
        detectedText = text.text
        _addEnabled.postValue(detectedText.isNotBlank())

        // Update detected text box
        _detectedRects.postValue(
            text.textBlocks
                .takeIf { it.isNotEmpty() }
                ?.let { blocks ->
                    listOf(Rect(
                        blocks.mapNotNull { it.boundingBox?.left }.min(),
                        blocks.mapNotNull { it.boundingBox?.top }.min(),
                        blocks.mapNotNull { it.boundingBox?.right }.max(),
                        blocks.mapNotNull { it.boundingBox?.bottom }.max()
                    ))
                }
                ?: emptyList()
        )
    }

    fun addDetectedText() {
        val text = detectedText.takeIf { it.isNotBlank() } ?: return
        val soFar: String = savedStateHandle[CONTENT_KEY] ?: ""
        savedStateHandle[CONTENT_KEY] = soFar + text
    }

    companion object {
        @VisibleForTesting
        const val CONTENT_KEY = "content"
    }

    class Factory(private val storage: KeyValueStorage) : AbstractSavedStateViewModelFactory() {
        override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(storage, handle) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}