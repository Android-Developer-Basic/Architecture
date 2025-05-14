package ru.otus.architecture

import android.graphics.Rect
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.Text

class MainViewModel : ViewModel() {
    private var detectedText: String = ""

    private val _content: MutableLiveData<String> = MutableLiveData("")
    val content: LiveData<String> get() = _content

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
}