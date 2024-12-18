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
    private val danmuHeight = context.resources.getDimensionPixelSize(R.dimen.danmu_height).toFloat()
    private val avatarSize = danmuHeight - 2 * context.resources.getDimensionPixelSize(R.dimen.danmu_padding_vertical).toFloat()
    private val paddingLeft = context.resources.getDimensionPixelSize(R.dimen.danmu_padding_left).toFloat()
    private val paddingRight = context.resources.getDimensionPixelSize(R.dimen.danmu_padding_right).toFloat()
    private val paddingVertical = context.resources.getDimensionPixelSize(R.dimen.danmu_padding_vertical).toFloat()
    private var onDanmuCompleteListener: ((DanmuItem) -> Unit)? = null

    private val allDanmuList = mutableListOf<DanmuItem>() // 存储所有弹幕
    private var currentIndex = 0 // 当前播放到的弹幕索引
    private val columnWidth = 300f // 每列的宽度

    private val safeDistance = context.resources.getDimensionPixelSize(R.dimen.danmu_safe_distance).toFloat()

    private val textSize = context.resources.getDimensionPixelSize(R.dimen.danmu_text_size).toFloat()
    private val contentTextColor = Color.parseColor("#333333")

    // 添加可视区域矩形
    private val visibleRect = RectF()

    init {
        paint.style = Paint.Style.FILL
        textPaint.textSize = textSize
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
                
                // 当弹幕完全离开可视区域时移除
                if (holder.x + holder.width < 0) {
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
            return
        }

        val y = rowIndex * (danmuHeight + paddingVertical) + paddingVertical + danmuHeight
        val textWidth = textPaint.measureText(danmu.username + "  " + danmu.content)
        val totalWidth = paddingLeft + avatarSize + paddingLeft + textWidth + 
                        (danmu.image?.let { avatarSize + paddingLeft } ?: 0f) + paddingRight

        // 确保新弹幕从屏幕右边开始
        val holder = DanmuViewHolder(
            danmuItem = danmu,
            x = visibleRect.right,
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
            // 只检查最后一个可见弹幕
            if (isVisible(lastDanmu)) {
                if (lastDanmu.x + lastDanmu.width + safeDistance < width) {
                    return i
                }
            } else {
                // 如果最后一个弹幕不可见，检查是否有足够空间
                if (lastDanmu.x + lastDanmu.width + safeDistance < width) {
                    return i
                }
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
            invalidate()
            return
        }

        val request = ImageRequest.Builder(context)
            .data(url)
            .transformations(CircleCropTransformation())
            .target { drawable ->
                val bitmap = drawable.toBitmap(
                    width = avatarSize.toInt(),
                    height = avatarSize.toInt(),
                    config = Bitmap.Config.ARGB_8888
                )
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
            invalidate()
            return
        }

        val request = ImageRequest.Builder(context)
            .data(url)
            .target { drawable ->
                val bitmap = drawable.toBitmap(
                    width = avatarSize.toInt(),
                    height = avatarSize.toInt(),
                    config = Bitmap.Config.ARGB_8888
                )
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
                // 检查弹幕是否在可视区域内
                if (isVisible(holder)) {
                    drawDanmu(canvas, holder)
                }
            }
        }
    }

    private fun drawDanmu(canvas: Canvas, holder: DanmuViewHolder) {
        // 绘制背景
        paint.style = Paint.Style.FILL
        paint.shader = DanmuConfig.createGradientShader(holder.width, holder.height)
        val cornerRadius = holder.height / 2
        canvas.drawRoundRect(holder.rect, cornerRadius, cornerRadius, paint)

        // 绘制边框
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor(DanmuConfig.BORDER_COLOR)
        canvas.drawRoundRect(holder.rect, cornerRadius, cornerRadius, paint)

        // 重置画笔样式为填充，以便正确绘制头像
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE // 重置颜色

        // 绘制头像
        holder.avatarBitmap?.let { avatar ->
            val avatarRect = RectF(
                holder.x + paddingLeft,
                holder.y - holder.height + paddingVertical,
                holder.x + paddingLeft + avatarSize,
                holder.y - paddingVertical
            )
            canvas.drawBitmap(avatar, null, avatarRect, paint)
        }

        // 绘制文本
        val textX = holder.x + paddingLeft + avatarSize + paddingLeft
        val textY = holder.y - holder.height/2 + textPaint.textSize/3

        // 绘制用户名
        textPaint.color = Color.parseColor(DanmuConfig.USERNAME_COLOR)
        textPaint.isFakeBoldText = true
        canvas.drawText(holder.danmuItem.username, textX, textY, textPaint)

        // 绘制内容
        val usernameWidth = textPaint.measureText(holder.danmuItem.username)
        textPaint.color = contentTextColor
        textPaint.isFakeBoldText = false
        canvas.drawText(holder.danmuItem.content, textX + usernameWidth + paddingLeft, textY, textPaint)

        // 绘制图片（如果有）
        holder.imageBitmap?.let { image ->
            val imageRect = RectF(
                textX + usernameWidth + textPaint.measureText(holder.danmuItem.content) + paddingLeft,
                holder.y - holder.height + paddingVertical,
                textX + usernameWidth + textPaint.measureText(holder.danmuItem.content) + paddingLeft + avatarSize,
                holder.y - paddingVertical
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

    // 添加新方法：检查弹幕是否在可视区域内
    private fun isVisible(holder: DanmuViewHolder): Boolean {
        // 创建弹幕的包围盒
        val danmuRect = RectF(
            holder.x,
            holder.y - holder.height,
            holder.x + holder.width,
            holder.y
        )
        // 检查是否与可视区域相交
        return RectF.intersects(visibleRect, danmuRect)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        visibleRect.set(0f, 0f, w.toFloat(), h.toFloat())
    }
} 