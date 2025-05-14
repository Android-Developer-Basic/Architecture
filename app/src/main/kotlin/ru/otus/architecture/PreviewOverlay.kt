package ru.otus.architecture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class PreviewOverlay(context: Context?, attributeSet: AttributeSet?) : View(context, attributeSet) {

    var rectBounds by Delegates.observable(emptyList<Rect>()) { _, _, _ -> invalidate() }

    private val paint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rectBounds.forEach { canvas.drawRect(it, paint) }
    }
}