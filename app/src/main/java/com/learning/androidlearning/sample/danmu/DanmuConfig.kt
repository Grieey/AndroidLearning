package com.learning.androidlearning.sample.danmu

import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader

data class DanmuItem(
    val avatar: String, // 头像URL或本地资源
    val username: String,
    val content: String,
    val image: String? = null // 可选的图片URL或本地资源
)

object DanmuConfig {
    const val CORNER_RADIUS = 25f
    const val BORDER_COLOR = "#FFBABA"
    const val GRADIENT_START_COLOR = "#FFF0D7"
    const val GRADIENT_END_COLOR = "#FFC4C4"
    const val GRADIENT_ANGLE = 30f
    const val USERNAME_COLOR = "#E95905"
    const val MAX_LINES = 3
    
    fun createGradientShader(width: Float, height: Float): Shader {
        return LinearGradient(
            0f, 0f,
            width * kotlin.math.cos(Math.toRadians(GRADIENT_ANGLE.toDouble())).toFloat(),
            height * kotlin.math.sin(Math.toRadians(GRADIENT_ANGLE.toDouble())).toFloat(),
            Color.parseColor(GRADIENT_START_COLOR),
            Color.parseColor(GRADIENT_END_COLOR),
            Shader.TileMode.CLAMP
        )
    }
} 