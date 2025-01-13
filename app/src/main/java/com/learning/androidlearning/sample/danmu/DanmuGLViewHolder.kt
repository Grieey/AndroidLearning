package com.learning.androidlearning.sample.danmu

import android.graphics.Bitmap
import android.graphics.RectF
import android.opengl.GLES20

class DanmuGLViewHolder(
        val danmuItem: DanmuItem,
        var x: Float,
        var y: Float,
        var width: Float,
        var height: Float,
        var textWidth: Float,
        private val paddingLeft: Float,
        private val avatarSize: Float,
        private val imageSize: Float,
        private val imageMarginLeft: Float
) {
    var rect = RectF()
    var avatarBitmap: Bitmap? = null
    var imageBitmap: Bitmap? = null
    var avatarTextureId: Int = -1
    var imageTextureId: Int = -1
    var backgroundTextureId: Int = -1

    // 背景顶点坐标
    var backgroundVertices: FloatArray = FloatArray(12)
    // 头像顶点坐标
    var avatarVertices: FloatArray = FloatArray(12)
    // 文本顶点坐标
    var textVertices: FloatArray = FloatArray(12)
    // 图片顶点坐标
    var imageVertices: FloatArray = FloatArray(12)

    // 纹理坐标（所有元素共用相同的纹理坐标）
    var textureCoords: FloatArray =
            FloatArray(8).apply {
                // 左上角
                this[0] = 0f
                this[1] = 0f
                // 左下角
                this[2] = 0f
                this[3] = 1f
                // 右上角
                this[4] = 1f
                this[5] = 0f
                // 右下角
                this[6] = 1f
                this[7] = 1f
            }

    fun updatePosition(deltaX: Float) {
        x += deltaX
        updateRect()
        updateVertices(x, y, width, height)

        // 更新其他元素的位置
        val avatarX = x + paddingLeft
        val avatarY = y + (height - avatarSize) / 2
        updateAvatarVertices(avatarX, avatarY, avatarSize)

        val textX = avatarX + avatarSize + paddingLeft
        updateTextVertices(textX, y, textWidth, height)

        danmuItem.image?.let {
            val imageX = textX + textWidth + imageMarginLeft
            val imageY = y + (height - imageSize) / 2
            updateImageVertices(imageX, imageY, imageSize)
        }
    }

    fun updateRect() {
        rect.set(x, y, x + width, y + height)
    }

    fun updateVertices(x: Float, y: Float, width: Float, height: Float) {
        // 左上角
        backgroundVertices[0] = x
        backgroundVertices[1] = y
        backgroundVertices[2] = 0f

        // 左下角
        backgroundVertices[3] = x
        backgroundVertices[4] = y + height
        backgroundVertices[5] = 0f

        // 右上角
        backgroundVertices[6] = x + width
        backgroundVertices[7] = y
        backgroundVertices[8] = 0f

        // 右下角
        backgroundVertices[9] = x + width
        backgroundVertices[10] = y + height
        backgroundVertices[11] = 0f
    }

    fun updateAvatarVertices(x: Float, y: Float, size: Float) {
        // 使用 TRIANGLE_STRIP 绘制顺序：左上、左下、右上、右下
        // 左上角
        avatarVertices[0] = x
        avatarVertices[1] = y
        avatarVertices[2] = 0f

        // 左下角
        avatarVertices[3] = x
        avatarVertices[4] = y + size
        avatarVertices[5] = 0f

        // 右上角
        avatarVertices[6] = x + size
        avatarVertices[7] = y
        avatarVertices[8] = 0f

        // 右下角
        avatarVertices[9] = x + size
        avatarVertices[10] = y + size
        avatarVertices[11] = 0f
    }

    fun updateTextVertices(x: Float, y: Float, width: Float, height: Float) {
        // 使用 TRIANGLE_STRIP 绘制顺序：左上、左下、右上、右下
        // 左上角
        textVertices[0] = x
        textVertices[1] = y
        textVertices[2] = 0f

        // 左下角
        textVertices[3] = x
        textVertices[4] = y + height
        textVertices[5] = 0f

        // 右上角
        textVertices[6] = x + width
        textVertices[7] = y
        textVertices[8] = 0f

        // 右下角
        textVertices[9] = x + width
        textVertices[10] = y + height
        textVertices[11] = 0f
    }

    fun updateImageVertices(x: Float, y: Float, size: Float) {
        // 使用 TRIANGLE_STRIP 绘制顺序：左上、左下、右上、右下
        // 左上角
        imageVertices[0] = x
        imageVertices[1] = y
        imageVertices[2] = 0f

        // 左下角
        imageVertices[3] = x
        imageVertices[4] = y + size
        imageVertices[5] = 0f

        // 右上角
        imageVertices[6] = x + size
        imageVertices[7] = y
        imageVertices[8] = 0f

        // 右下角
        imageVertices[9] = x + size
        imageVertices[10] = y + size
        imageVertices[11] = 0f
    }

    fun release() {
        if (backgroundTextureId != -1) {
            GLES20.glDeleteTextures(1, intArrayOf(backgroundTextureId), 0)
            backgroundTextureId = -1
        }
        if (avatarTextureId != -1) {
            GLES20.glDeleteTextures(1, intArrayOf(avatarTextureId), 0)
            avatarTextureId = -1
        }
        if (imageTextureId != -1) {
            GLES20.glDeleteTextures(1, intArrayOf(imageTextureId), 0)
            imageTextureId = -1
        }
        avatarBitmap?.recycle()
        imageBitmap?.recycle()
        avatarBitmap = null
        imageBitmap = null
    }

    companion object {
        private const val paddingLeft = 16f
        private const val avatarSize = 40f
        private const val imageSize = 40f
        private const val imageMarginLeft = 8f
    }
}
