package ru.otus.architecture

import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.Text.Line
import com.google.mlkit.vision.text.Text.TextBlock
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun returnsInitialState() {
        val viewModel = MainViewModel()
        assert(viewModel.content.value == "")
        assert(viewModel.shareEnabled.value == false)
        assert(viewModel.addEnabled.value == false)
        assert(viewModel.detectedRects.value?.isEmpty() == true)
    }

    @Test
    fun enablesAddButtonOnRecognizedText() {
        val observedValues = mutableListOf<Boolean>()

        val viewModel = MainViewModel()
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

        val viewModel = MainViewModel()
        viewModel.detectedRects.observeForever {
            observedValues.add(it)
        }

        viewModel.updateRecognizedText(listOf("Hello".toTextBlock(RECT_1), "World".toTextBlock(RECT_2)).toText())

        assert(observedValues == listOf(emptyList(), listOf(expectedRect)))
    }

    @Test
    fun addsDetectedText() {
        val observedValues = mutableListOf<String>()

        val viewModel = MainViewModel()
        viewModel.content.observeForever {
            observedValues.add(it)
        }

        viewModel.updateRecognizedText(Text("Hello"))
        viewModel.addDetectedText()

        assert(observedValues == listOf("", "Hello"))
    }

    @Test
    fun enablesShareWhenContentIsNotEmpty() {
        val observedValues = mutableListOf<Boolean>()

        val viewModel = MainViewModel()
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