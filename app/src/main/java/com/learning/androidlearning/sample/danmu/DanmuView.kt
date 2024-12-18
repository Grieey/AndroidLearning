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

    private val allDanmuList = mutableListOf<DanmuItem>() // 存储所有弹幕
    private var currentIndex = 0 // 当前播放到的弹幕索引
    private val columnWidth = 300f // 每列的宽度

    private val safeDistance = context.resources.getDimensionPixelSize(R.dimen.danmu_safe_distance).toFloat()

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
        if (rowIndex < 0) {
            // 如果没有安全的行，暂时不添加这个弹幕
            return
        }

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
        for (i in 0 until DanmuConfig.MAX_LINES) {
            val row = danmuRows[i]
            if (row.isEmpty()) {
                return i
            }
            
            val lastDanmu = row.last()
            // 只需要确保与最后一个弹幕的间距是12dp
            if (lastDanmu.x + lastDanmu.width + safeDistance < width) {
                return i
            }
        }
        return -1
    }

    private fun isRowSafe(rowIndex: Int): Boolean {
        val row = danmuRows[rowIndex]
        if (row.isEmpty()) return true

        val lastDanmu = row.last()
        // 只检查与最后一个弹幕的间距
        return lastDanmu.x + lastDanmu.width + safeDistance < width
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

    // 添加新方法：设置弹幕列表
    fun setDanmuList(danmuList: List<DanmuItem>) {
        allDanmuList.clear()
        allDanmuList.addAll(danmuList)
        currentIndex = 0
        clearCurrentDanmu()
        startDisplayingDanmu()
    }

    // 添加新方法：重新播放
    fun replay() {
        currentIndex = 0
        clearCurrentDanmu()
        startDisplayingDanmu()
    }

    private fun clearCurrentDanmu() {
        danmuRows.forEach { it.clear() }
        invalidate()
    }

    private fun startDisplayingDanmu() {
        postDelayed(object : Runnable {
            override fun run() {
                if (currentIndex < allDanmuList.size) {
                    addNextColumnDanmu()
                    // 根据弹幕密度动态调整延迟时间
                    val delay = calculateNextDelay()
                    postDelayed(this, delay)
                }
            }
        }, calculateNextDelay())
    }

    private fun addNextColumnDanmu() {
        var addedCount = 0
        while (addedCount < 3 && currentIndex < allDanmuList.size) {
            val rowIndex = findBestRow()
            if (rowIndex >= 0) {
                addDanmu(allDanmuList[currentIndex])
                currentIndex++
                addedCount++
            } else {
                // 如果当前没有合适的行，等待下一次尝试
                break
            }
        }
    }

    private fun calculateNextDelay(): Long {
        return 300L // 使用固定的延迟时间，确保弹幕间距更均匀
    }
} 