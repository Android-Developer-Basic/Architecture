package ru.otus.architecture

import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.Line
import com.google.mlkit.vision.text.Text.TextBlock
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.otus.architecture.prefs.KeyValueStorage

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var storage: KeyValueStorage
    private lateinit var prefix: MutableLiveData<String?>
    private lateinit var viewModel: MainViewModel

    @Before
    fun init() {
        prefix = MutableLiveData(null)
        storage = mockk {
            every { this@mockk.liveData(any()) } returns prefix
            every { this@mockk.get(any()) } returns prefix.value
        }
        viewModel = MainViewModel(storage)
    }

    @Test
    fun returnsInitialState() {
        assert(viewModel.content.value == "")
        assert(viewModel.shareEnabled.value == false)
        assert(viewModel.addEnabled.value == false)
        assert(viewModel.detectedRects.value?.isEmpty() == true)
    }

    @Test
    fun enablesAddButtonOnRecognizedText() {
        val observedValues = mutableListOf<Boolean>()

        viewModel.addEnabled.observeForever {
            observedValues.add(it)
        }

        viewModel.updateRecognizedText(Text("Hello"))

        assert(observedValues == listOf(false, true))
    }

    @Test
    fun updatesRectangleOnRecognizedText() {
        val expectedRect = Rect(
            RECT_2.left,
            RECT_1.top,
            RECT_2.right,
            RECT_1.bottom
        )

        val observedValues = mutableListOf<List<Rect>>()

        viewModel.detectedRects.observeForever {
            observedValues.add(it)
        }

        viewModel.updateRecognizedText(listOf("Hello".toTextBlock(RECT_1), "World".toTextBlock(RECT_2)).toText())

        assert(observedValues == listOf(emptyList(), listOf(expectedRect)))
    }

    @Test
    fun addsDetectedText() {
        val observedValues = mutableListOf<String>()

        viewModel.content.observeForever {
            observedValues.add(it)
        }

        viewModel.updateRecognizedText(Text("Hello"))
        viewModel.addDetectedText()

        assert(observedValues == listOf("", "Hello"))
    }

    @Test
    fun changesContentWhenPrefixChanges() {
        val observedValues = mutableListOf<String>()

        viewModel.content.observeForever {
            observedValues.add(it)
        }

        viewModel.updateRecognizedText(Text("Hello"))
        viewModel.addDetectedText()
        prefix.postValue("Prefix")

        assert(observedValues == listOf("", "Hello", "Prefix\nHello"))
    }

    @Test
    fun enablesShareWhenContentIsNotEmpty() {
        val observedValues = mutableListOf<Boolean>()

        viewModel.shareEnabled.observeForever {
            observedValues.add(it)
        }

        viewModel.updateRecognizedText(Text("Hello"))
        viewModel.addDetectedText()

        assert(observedValues == listOf(false, true))
    }

    private companion object {
        private val RECT_1 = Rect(140, 156, 345, 196)
        private val RECT_2 = Rect(139, 157, 346, 195)

        fun String.toTextLine(rect: Rect = Rect()): Line = Line(
            this,
            rect,
            emptyList<Point>(),
            "und",
            Matrix.IDENTITY_MATRIX,
            emptyList<String>(),
            0F,
            0F
        )

        fun String.toTextBlock(rect: Rect = RECT_1, moreLines: List<Line> = emptyList()): TextBlock = TextBlock(
            this,
            rect,
            with(rect) {
                listOf(
                    Point(top, left),
                    Point(top, right),
                    Point(bottom, right),
                    Point(bottom, left),
                )
            },
            "und",
            Matrix.IDENTITY_MATRIX,
            listOf(this.toTextLine(rect)) + moreLines
        )

        fun List<TextBlock>.toText(): Text = Text(
            this.joinToString("\n") { it.text },
            this
        )

        fun TextBlock.toText(): Text = listOf(this).toText()

        @Suppress("TestFunctionName")
        fun Text(string: String): Text = string.toTextBlock().toText()
    }
}