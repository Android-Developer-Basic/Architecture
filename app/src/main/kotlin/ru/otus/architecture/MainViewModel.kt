package ru.otus.architecture

import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import com.google.mlkit.vision.text.Text
import ru.otus.architecture.prefs.KeyValueStorage

class MainViewModel(private val storage: KeyValueStorage) : ViewModel() {
    private var detectedText: String = ""

    private val _content: MutableLiveData<String> = MutableLiveData("")
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

    private val _shareEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    val shareEnabled: LiveData<Boolean> get() = _shareEnabled

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
        _content.postValue(_content.value + text)
        _shareEnabled.postValue(true)
    }

    class Factory(private val storage: KeyValueStorage) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(storage) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}