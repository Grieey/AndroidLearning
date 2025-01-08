package com.learning.androidlearning.sample.gradient

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View

class GradientTextView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var text = "Gradient Text"
    private var textSize = 60f
    private val charBackgrounds = mutableListOf<RectF>()
    private val cornerRadius = 16f
    private val charPadding = 8f

    init {
        textPaint.apply {
            textSize = this@GradientTextView.textSize
            textAlign = Paint.Align.LEFT
            color = Color.WHITE
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateCharBackgrounds()
    }

    private fun updateCharBackgrounds() {
        charBackgrounds.clear()
        var currentX = paddingLeft.toFloat()

        text.forEach { char ->
            val charWidth = textPaint.measureText(char.toString())
            val charHeight = textPaint.descent() - textPaint.ascent()
            val yPos = height / 2f - charHeight / 2f - charPadding

            val rect =
                    RectF(
                            currentX,
                            yPos,
                            currentX + charWidth + charPadding * 2,
                            yPos + charHeight + charPadding * 2
                    )
            charBackgrounds.add(rect)

            currentX += charWidth + charPadding * 2 + charPadding
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val charWidths = text.map { textPaint.measureText(it.toString()) }
        val totalWidth =
                charWidths.sum() +
                        charPadding * 2 * text.length +
                        charPadding * (text.length - 1) +
                        paddingLeft +
                        paddingRight

        val charHeight = textPaint.descent() - textPaint.ascent()
        val desiredHeight = charHeight + charPadding * 4 + paddingTop + paddingBottom

        val width = resolveSize(totalWidth.toInt(), widthMeasureSpec)
        val height = resolveSize(desiredHeight.toInt(), heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        text.forEachIndexed { index, char ->
            val rect = charBackgrounds[index]

            // Create gradient for each character background
            backgroundPaint.shader =
                    LinearGradient(
                            rect.left,
                            rect.top,
                            rect.left,
                            rect.bottom,
                            0x33FFE187.toInt(),
                            0x00FFE187.toInt(),
                            Shader.TileMode.CLAMP
                    )

            // Draw rounded rectangle background
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, backgroundPaint)

            // Draw character
            val xPos = rect.left + charPadding
            val yPos = rect.centerY() - (textPaint.descent() + textPaint.ascent()) / 2
            canvas.drawText(char.toString(), xPos, yPos, textPaint)
        }
    }

    fun setText(newText: String) {
        text = newText
        updateCharBackgrounds()
        invalidate()
        requestLayout()
    }

    fun setTextSize(size: Float) {
        textSize = size
        textPaint.textSize = size
        updateCharBackgrounds()
        invalidate()
        requestLayout()
    }
}
