package com.learning.androidlearning.sample.danmu

import android.graphics.Bitmap
import android.graphics.RectF

/**
 * 弹幕视图持有者，用于管理单个弹幕的状态和绘制信息
 */
data class DanmuViewHolder(
    val danmuItem: DanmuItem,
    var x: Float = 0f,
    var y: Float = 0f,
    var width: Float = 0f,
    var height: Float = 0f,
    var rect: RectF = RectF(),
    var avatarBitmap: Bitmap? = null,
    var imageBitmap: Bitmap? = null,
    var isVisible: Boolean = true
) {
    /**
     * 更新弹幕的矩形区域
     */
    fun updateRect() {
        rect.set(x, y - height, x + width, y)
    }

    /**
     * 检查弹幕是否已经完全离开屏幕
     */
    fun isOutOfScreen(): Boolean {
        return x + width < 0
    }

    /**
     * 更新弹幕的位置
     */
    fun updatePosition(dx: Float) {
        x += dx
        updateRect()
    }
} 