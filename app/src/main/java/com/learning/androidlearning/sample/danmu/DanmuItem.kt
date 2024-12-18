package com.learning.androidlearning.sample.danmu

data class DanmuItem(
    val avatar: String,
    val username: String,
    val content: String,
    val image: String? = null,
    val style: Int = STYLE_1 // 使用样式标记
) {
    companion object {
        const val STYLE_1 = 1 // 有边框，显示用户名
        const val STYLE_2 = 2 // 无边框，不显示用户名
    }
} 