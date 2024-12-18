package com.learning.androidlearning.sample.danmu

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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

    private var animator: ValueAnimator? = null
    private val scrollSpeed = 4f // 像素/毫秒
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

    // 添加图片尺寸常量
    private val imageSize = context.resources.getDimensionPixelSize(R.dimen.danmu_image_size).toFloat()

    // 在 DanmuView 类中添加常量
    private val imageMarginLeft = context.resources.getDimensionPixelSize(R.dimen.danmu_image_margin_left).toFloat()

    // 添加行间距属性
    private val rowSpacing = context.resources.getDimensionPixelSize(R.dimen.danmu_row_spacing).toFloat()

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
        var needsRedraw = false

        for (row in danmuRows.indices) {
            val currentRow = danmuRows[row]

            // 移除已经离开屏幕的弹幕
            currentRow.removeAll { holder ->
                val shouldRemove = holder.isOutOfScreen()
                if (shouldRemove) {
                    onDanmuCompleteListener?.invoke(holder.danmuItem)
                }
                shouldRemove
            }

            // 更新剩余弹幕的位置
            for (i in currentRow.indices) {
                val holder = currentRow[i]
                val prevHolder = if (i > 0) currentRow[i - 1] else null

                // 更新位置
                holder.updatePosition(-scrollSpeed)

                // 如果有前一个弹幕，检查间距
                prevHolder?.let { prev ->
                    val currentDistance = holder.x - (prev.x + prev.width)
                    if (currentDistance < safeDistance) {
                        // 如果间距小于安全距离，调整位置
                        holder.x = prev.x + prev.width + safeDistance
                        holder.updateRect()
                    }
                }

                needsRedraw = true
            }
        }

        if (needsRedraw) {
            invalidate()
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

        // 修改 y 坐标的计算，确保第一行弹幕完整显示
        val y = (rowIndex + 1) * (danmuHeight + rowSpacing) // 从第一个完整位置开始
        val textWidth = textPaint.measureText(danmu.username + "  " + danmu.content)
        val totalWidth = paddingLeft + avatarSize + paddingLeft + textWidth +
                (danmu.image?.let { imageSize + imageMarginLeft } ?: 0f) + paddingRight

        // 计算新弹幕的起始 x 坐标
        var startX = width.toFloat() // 默认从屏幕右边开始
        val row = danmuRows[rowIndex]
        if (row.isNotEmpty()) {
            // 获取同行最后一个弹幕
            val lastDanmu = row.last()
            // 新弹幕的起始位置 = 最后一个弹幕的右边界 + 安全距离
            startX = lastDanmu.x + lastDanmu.width + safeDistance
        }

        val holder = DanmuViewHolder(
            danmuItem = danmu,
            x = startX,
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
            // 计算最后一个弹幕的右边界到屏幕右边界的距离
            val distanceToRight = width - (lastDanmu.x + lastDanmu.width)

            // 如果距离大于安全距离，则可以添加新弹幕
            if (distanceToRight >= safeDistance) {
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
            invalidate()
            return
        }

        Glide.with(context)
            .asBitmap()
            .load(url)
            .override(avatarSize.toInt(), avatarSize.toInt())
            .circleCrop() // Glide 4.11.0 的圆形裁剪
            .into(object : CustomTarget<Bitmap>(avatarSize.toInt(), avatarSize.toInt()) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // 创建一个新的 Bitmap，确保填充整个区域
                    val targetSize = avatarSize.toInt()
                    val scale = maxOf(
                        targetSize.toFloat() / resource.width,
                        targetSize.toFloat() / resource.height
                    )
                    val scaledWidth = (resource.width * scale).toInt()
                    val scaledHeight = (resource.height * scale).toInt()
                    
                    val scaledBitmap = Bitmap.createScaledBitmap(
                        resource,
                        scaledWidth,
                        scaledHeight,
                        true
                    )
                    
                    // 居中裁剪
                    val x = (scaledWidth - targetSize) / 2
                    val y = (scaledHeight - targetSize) / 2
                    val finalBitmap = Bitmap.createBitmap(
                        scaledBitmap,
                        maxOf(0, x),
                        maxOf(0, y),
                        targetSize,
                        targetSize
                    )
                    
                    avatarCache[url] = finalBitmap
                    holder.avatarBitmap = finalBitmap
                    invalidate()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // 处理加载被清除的情况
                }
            })
    }

    private fun loadImage(url: String, holder: DanmuViewHolder) {
        // 对于本地图片资源，直接从资源加载
        if (url == "ic_red_packet") {
            val resourceId = context.resources.getIdentifier(url, "drawable", context.packageName)
            if (resourceId != 0) {
                Glide.with(context)
                    .asBitmap()
                    .load(resourceId)
                    .override(imageSize.toInt(), imageSize.toInt())
                    .into(object : CustomTarget<Bitmap>(imageSize.toInt(), imageSize.toInt()) {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            holder.imageBitmap = resource
                            invalidate()
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // 处理加载被清除的情况
                        }
                    })
            }
            return
        }

        // 其他远程图片的加载逻辑
        if (imageCache.containsKey(url)) {
            holder.imageBitmap = imageCache[url]
            invalidate()
            return
        }

        Glide.with(context)
            .asBitmap()
            .load(url)
            .override(imageSize.toInt(), imageSize.toInt())
            .error(R.drawable.default_image)
            .fallback(R.drawable.default_image)
            .into(object : CustomTarget<Bitmap>(imageSize.toInt(), imageSize.toInt()) {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageCache[url] = resource
                    holder.imageBitmap = resource
                    invalidate()
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // 处理加载被清除的情况
                }
            })
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
        paint.shader = DanmuConfig.createGradientShader(holder.width, holder.height, holder.danmuItem.hasBorder)
        val cornerRadius = holder.height / 2
        canvas.drawRoundRect(holder.rect, cornerRadius, cornerRadius, paint)

        // 只有在需要边框时才绘制
        if (holder.danmuItem.hasBorder) {
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor(DanmuConfig.BORDER_COLOR)
            canvas.drawRoundRect(holder.rect, cornerRadius, cornerRadius, paint)
        }

        // 重置画笔样式为填充，以便正确绘制头像
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE // 重置颜色

        // 绘制头像
        holder.avatarBitmap?.let { avatar ->
            val avatarRect = RectF(
                holder.x + paddingLeft,
                holder.y - holder.height,  // 修改头像的垂直位置
                holder.x + paddingLeft + avatarSize,
                holder.y  // 修改头像的垂直位置
            )

            // 保存画布状态
            canvas.save()

            // 创建圆形裁剪区域
            val path = Path().apply {
                addCircle(
                    avatarRect.centerX(),
                    avatarRect.centerY(),
                    avatarSize / 2,
                    Path.Direction.CW
                )
            }
            canvas.clipPath(path)

            // 绘制头像
            canvas.drawBitmap(avatar, null, avatarRect, paint)

            // 恢复画布状态
            canvas.restore()
        }

        // 绘制文本
        val textX = holder.x + paddingLeft + avatarSize + paddingLeft
        val textY = holder.y - holder.height/2 + textSize/3  // 调整文本的垂直位置

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
                textX + usernameWidth + textPaint.measureText(holder.danmuItem.content) + imageMarginLeft,
                holder.y - holder.height/2 - imageSize/2,  // 调整图片的垂直位置
                textX + usernameWidth + textPaint.measureText(holder.danmuItem.content) + imageMarginLeft + imageSize,
                holder.y - holder.height/2 + imageSize/2  // 调整图片的垂直位置
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