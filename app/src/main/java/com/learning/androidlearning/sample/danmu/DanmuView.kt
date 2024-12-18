package com.learning.androidlearning.sample.danmu

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.learning.androidlearning.R

class DanmuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val danmuRows = Array(DanmuConfig.MAX_LINES) { mutableListOf<DanmuViewHolder>() }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val avatarCache = mutableMapOf<String, Bitmap>()
    private val imageCache = mutableMapOf<String, Bitmap>()
    private val imageLoader = ImageLoader(context)

    private var animator: ValueAnimator? = null
    private val scrollSpeed = 2f // 像素/毫秒
    private val danmuHeight = 100f
    private val avatarSize = 80f
    private val padding = 10f
    private var onDanmuCompleteListener: ((DanmuItem) -> Unit)? = null

    init {
        paint.style = Paint.Style.FILL
        textPaint.textSize = 40f
        startScrollAnimation()
    }

    private fun startScrollAnimation() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 16 // 16ms per frame for 60fps
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                updateDanmuPositions()
                invalidate()
            }
            start()
        }
    }

    private fun updateDanmuPositions() {
        danmuRows.forEach { row ->
            val iterator = row.iterator()
            while (iterator.hasNext()) {
                val holder = iterator.next()
                holder.updatePosition(-scrollSpeed)
                
                if (holder.isOutOfScreen()) {
                    iterator.remove()
                    onDanmuCompleteListener?.invoke(holder.danmuItem)
                }
            }
        }
    }

    fun setOnDanmuCompleteListener(listener: (DanmuItem) -> Unit) {
        onDanmuCompleteListener = listener
    }

    fun addDanmu(danmu: DanmuItem) {
        val rowIndex = findBestRow()
        val y = rowIndex * (danmuHeight + padding) + padding + danmuHeight

        val textWidth = textPaint.measureText(danmu.username + "  " + danmu.content)
        val totalWidth = avatarSize + padding + textWidth + (danmu.image?.let { avatarSize } ?: 0f)

        val holder = DanmuViewHolder(
            danmuItem = danmu,
            x = width.toFloat(),
            y = y,
            width = totalWidth,
            height = danmuHeight
        )
        holder.updateRect()

        danmuRows[rowIndex].add(holder)
        loadAvatar(danmu.avatar, holder)
        danmu.image?.let { loadImage(it, holder) }
        invalidate()
    }

    private fun findBestRow(): Int {
        var bestRow = 0
        var maxSpace = Float.NEGATIVE_INFINITY

        danmuRows.forEachIndexed { index, row ->
            val lastDanmuEnd = row.lastOrNull()?.let { it.x + it.width } ?: 0f
            val space = width - lastDanmuEnd
            if (space > maxSpace) {
                maxSpace = space
                bestRow = index
            }
        }

        return bestRow
    }

    private fun loadAvatar(url: String, holder: DanmuViewHolder) {
        if (avatarCache.containsKey(url)) {
            holder.avatarBitmap = avatarCache[url]
            return
        }

        val request = ImageRequest.Builder(context)
            .data(url)
            .transformations(CircleCropTransformation())
            .target { drawable ->
                val bitmap = drawable.toBitmap()
                avatarCache[url] = bitmap
                holder.avatarBitmap = bitmap
                invalidate()
            }
            .error(R.drawable.default_avatar)
            .fallback(R.drawable.default_avatar)
            .build()
        imageLoader.enqueue(request)
    }

    private fun loadImage(url: String, holder: DanmuViewHolder) {
        if (imageCache.containsKey(url)) {
            holder.imageBitmap = imageCache[url]
            return
        }

        val request = ImageRequest.Builder(context)
            .data(url)
            .target { drawable ->
                val bitmap = drawable.toBitmap()
                imageCache[url] = bitmap
                holder.imageBitmap = bitmap
                invalidate()
            }
            .error(R.drawable.default_image)
            .fallback(R.drawable.default_image)
            .build()
        imageLoader.enqueue(request)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        danmuRows.forEach { row ->
            row.forEach { holder ->
                drawDanmu(canvas, holder)
            }
        }
    }

    private fun drawDanmu(canvas: Canvas, holder: DanmuViewHolder) {
        // 绘制背景
        paint.style = Paint.Style.FILL
        paint.shader = DanmuConfig.createGradientShader(holder.width, holder.height)
        canvas.drawRoundRect(holder.rect, DanmuConfig.CORNER_RADIUS, DanmuConfig.CORNER_RADIUS, paint)

        // 绘制边框
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor(DanmuConfig.BORDER_COLOR)
        canvas.drawRoundRect(holder.rect, DanmuConfig.CORNER_RADIUS, DanmuConfig.CORNER_RADIUS, paint)

        // 绘制头像
        holder.avatarBitmap?.let { avatar ->
            val avatarRect = RectF(
                holder.x + padding,
                holder.y - holder.height + padding,
                holder.x + padding + avatarSize,
                holder.y - padding
            )
            canvas.drawBitmap(avatar, null, avatarRect, paint)
        }

        // 绘制文本
        val textX = holder.x + avatarSize + padding * 2
        val textY = holder.y - holder.height / 2 + textPaint.textSize / 2

        // 绘制用户名
        textPaint.color = Color.parseColor(DanmuConfig.USERNAME_COLOR)
        textPaint.isFakeBoldText = true
        canvas.drawText(holder.danmuItem.username, textX, textY, textPaint)

        // 绘制内容
        val usernameWidth = textPaint.measureText(holder.danmuItem.username)
        textPaint.color = Color.BLACK
        textPaint.isFakeBoldText = false
        canvas.drawText(holder.danmuItem.content, textX + usernameWidth + padding, textY, textPaint)

        // 绘制图片（如果有）
        holder.imageBitmap?.let { image ->
            val imageRect = RectF(
                textX + usernameWidth + textPaint.measureText(holder.danmuItem.content) + padding * 2,
                holder.y - holder.height + padding,
                textX + usernameWidth + textPaint.measureText(holder.danmuItem.content) + padding * 2 + avatarSize,
                holder.y - padding
            )
            canvas.drawBitmap(image, null, imageRect, paint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator = null
    }
} 