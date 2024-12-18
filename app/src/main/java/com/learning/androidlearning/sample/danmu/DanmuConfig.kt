package com.learning.androidlearning.sample.danmu

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import kotlin.math.cos
import kotlin.math.sin

object DanmuConfig {
    const val MAX_LINES = 3
    const val BORDER_COLOR = "#FFBABA"
    const val GRADIENT_START_COLOR = "#FFF0D7"
    const val GRADIENT_END_COLOR = "#FFC4C4"
    const val GRADIENT_START_COLOR_NO_BORDER = "#99FFD7D7"
    const val GRADIENT_END_COLOR_NO_BORDER = "#99FFC4C4"
    const val USERNAME_COLOR = "#E95905"
    const val CONTENT_TEXT_COLOR = "#333333"

    fun createGradientShader(width: Float, height: Float, hasBorder: Boolean = true): LinearGradient {
        val colors = if (hasBorder) {
            intArrayOf(
                Color.parseColor(GRADIENT_START_COLOR),
                Color.parseColor(GRADIENT_END_COLOR)
            )
        } else {
            intArrayOf(
                Color.parseColor(GRADIENT_START_COLOR_NO_BORDER),
                Color.parseColor(GRADIENT_END_COLOR_NO_BORDER)
            )
        }

        val angle = 30f * Math.PI.toFloat() / 180f
        return LinearGradient(
            0f,
            0f,
            width * cos(angle),
            height * sin(angle),
            colors,
            null,
            Shader.TileMode.CLAMP
        )
    }
} 