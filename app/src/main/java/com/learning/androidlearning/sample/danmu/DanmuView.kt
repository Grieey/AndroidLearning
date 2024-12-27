package com.learning.androidlearning.sample.danmu

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.learning.androidlearning.R
import kotlin.math.abs

interface DanmuPlayCompleteListener {
    fun onDanmuPlayComplete()
}

class DanmuView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    private val danmuRows = Array(DanmuConfig.MAX_LINES) { mutableListOf<DanmuViewHolder>() }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val avatarCache = mutableMapOf<String, Bitmap>()
    private val imageCache = mutableMapOf<String, Bitmap>()

    private var animator: ValueAnimator? = null
    private var lastFrameTime = 0L
    private val scrollSpeed = 200f // 每秒移动的像素数
    private val danmuHeight =
            context.resources.getDimensionPixelSize(R.dimen.danmu_height).toFloat()
    private val avatarSize =
            danmuHeight -
                    2 *
                            context.resources
                                    .getDimensionPixelSize(R.dimen.danmu_padding_vertical)
                                    .toFloat()
    private val paddingLeft =
            context.resources.getDimensionPixelSize(R.dimen.danmu_padding_left).toFloat()
    private val paddingRight =
            context.resources.getDimensionPixelSize(R.dimen.danmu_padding_right).toFloat()
    private var onDanmuCompleteListener: ((DanmuItem) -> Unit)? = null

    private val allDanmuList = mutableListOf<DanmuItem>() // 存储所有弹幕
    private var currentIndex = 0 // 当前播放到的弹幕索引

    private val safeDistance =
            context.resources.getDimensionPixelSize(R.dimen.danmu_safe_distance).toFloat()

    private val textSize =
            context.resources.getDimensionPixelSize(R.dimen.danmu_text_size).toFloat()
    private val contentTextColor = Color.parseColor("#333333")

    // 添加可视区域矩形
    private val visibleRect = RectF()

    // 添加图片尺寸常量
    private val imageSize =
            context.resources.getDimensionPixelSize(R.dimen.danmu_image_size).toFloat()

    // 在 DanmuView 类中添加常量
    private val imageMarginLeft =
            context.resources.getDimensionPixelSize(R.dimen.danmu_image_margin_left).toFloat()

    // 添加行间距属性
    private val rowSpacing =
            context.resources.getDimensionPixelSize(R.dimen.danmu_row_spacing).toFloat()

    private var isAnimating = false
    private var isPaused = false
    private var pausedTime = 0L
    private var accumulatedTime = 0L

    private var onNeedMoreDanmuListener: (() -> Unit)? = null

    private var onDanmuClickListener: ((DanmuItem) -> Unit)? = null

    // 添加触摸相关的属性
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var touchedDanmu: DanmuViewHolder? = null
    private val touchSlop = 10f // 触摸容差，防止轻微移动也触发点击

    // 添加监听器变量
    private var danmuPlayCompleteListener: DanmuPlayCompleteListener? = null

    init {
        // 开启硬件加速
        setLayerType(LAYER_TYPE_HARDWARE, null)

        paint.isAntiAlias = true
        textPaint.isAntiAlias = true

        // 预创建对象，避免在绘制时创建
        paint.isDither = true
        textPaint.isDither = true

        paint.style = Paint.Style.FILL
        textPaint.textSize = textSize
        startScrollAnimation()
    }

    private fun startScrollAnimation() {
        if (isAnimating) return
        isAnimating = true
        lastFrameTime = System.nanoTime()
        accumulatedTime = 0

        animator?.cancel()
        animator =
                ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 16 // 16ms per frame for 60fps
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    addUpdateListener {
                        if (!isPaused) {
                            val currentTime = System.nanoTime()
                            val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f
                            lastFrameTime = currentTime

                            // 使用累积时间来平滑动画
                            accumulatedTime += (deltaTime * 1_000_000_000).toLong()
                            val distance = scrollSpeed * deltaTime

                            // 使用 postInvalidateOnAnimation 代替 invalidate
                            updateDanmuPositions(distance)
                            postInvalidateOnAnimation()
                        }
                    }
                    start()
                }
    }

    private fun updateDanmuPositions(distance: Float) {
        var needsRedraw = false
        var hasVisibleDanmu = false
        var maxX = Float.MIN_VALUE

        for (row in danmuRows.indices) {
            val currentRow = danmuRows[row]
            val iterator = currentRow.iterator()
            var prevHolder: DanmuViewHolder? = null

            while (iterator.hasNext()) {
                val holder = iterator.next()
                if (holder.x + holder.width < -width) {
                    iterator.remove()
                    onDanmuCompleteListener?.invoke(holder.danmuItem)
                    continue
                }

                holder.updatePosition(-distance)

                // 检查与前一个弹幕的间距
                prevHolder?.let { prev ->
                    val currentDistance = holder.x - (prev.x + prev.width)
                    if (currentDistance < safeDistance) {
                        // 如果间距小于安全距离，调整位置
                        holder.x = prev.x + prev.width + safeDistance
                        holder.updateRect()
                    }
                }
                prevHolder = holder

                if (holder.x + holder.width > 0) {
                    hasVisibleDanmu = true
                    maxX = maxOf(maxX, holder.x + holder.width)
                    needsRedraw = true
                }
            }
        }

        if (hasVisibleDanmu && maxX < width * 1.5) {
            onNeedMoreDanmuListener?.invoke()
        }

        if (needsRedraw) {
            postInvalidateOnAnimation()
        }
    }

    fun setOnDanmuCompleteListener(listener: (DanmuItem) -> Unit) {
        onDanmuCompleteListener = listener
    }

    fun addDanmu(danmu: DanmuItem) {
        // 找到最适合的行
        val rowIndex = findBestRow()
        val row = danmuRows[rowIndex]

        val y = rowSpacing + (rowIndex * (danmuHeight + rowSpacing)) + danmuHeight

        // 计算宽度
        val textWidth =
                if (danmu.style == DanmuItem.STYLE_1) {
                    textPaint.measureText(danmu.username + "  " + danmu.content)
                } else {
                    textPaint.measureText(danmu.content)
                }

        val totalWidth =
                paddingLeft +
                        avatarSize +
                        paddingLeft +
                        textWidth +
                        (danmu.image?.let { imageSize + imageMarginLeft } ?: 0f) +
                        paddingRight

        // 计算起始 x 坐标
        var startX = width.toFloat()

        // 如果该行已有弹幕，确保新弹幕与最后一个弹幕保持安全距离
        if (row.isNotEmpty()) {
            val lastDanmu = row.last()
            startX = maxOf(startX, lastDanmu.x + lastDanmu.width + safeDistance)
        }

        val holder =
                DanmuViewHolder(
                        danmuItem = danmu,
                        x = startX,
                        y = y,
                        width = totalWidth,
                        height = danmuHeight
                )
        holder.updateRect()

        // 添加到对应行
        row.add(holder)

        // 加载图片
        loadAvatar(danmu.avatar, holder)
        danmu.image?.let { loadImage(it, holder) }

        invalidate()
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
                .circleCrop()
                .into(
                        object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                            ) {
                                // 直接使用 Glide 的 circleCrop 后的位图，不需要再次处理
                                avatarCache[url] = resource
                                holder.avatarBitmap = resource
                                invalidate()
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // 处理加载被清除的情况
                            }
                        }
                )
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
                        .into(
                                object :
                                        CustomTarget<Bitmap>(imageSize.toInt(), imageSize.toInt()) {
                                    override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: Transition<in Bitmap>?
                                    ) {
                                        holder.imageBitmap = resource
                                        invalidate()
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        // 处理加载被清除的情况
                                    }
                                }
                        )
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
                .into(
                        object : CustomTarget<Bitmap>(imageSize.toInt(), imageSize.toInt()) {
                            override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                            ) {
                                imageCache[url] = resource
                                holder.imageBitmap = resource
                                invalidate()
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                // 处理加载被清除的情况
                            }
                        }
                )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var allDanmusGone = true
        var hasVisibleDanmus = false

        // 只绘制可见区域的弹幕
        val visibleLeft = -width.toFloat()
        val visibleRight = width * 2f

        danmuRows.forEach { row ->
            row.forEach { holder ->
                if (holder.x <= visibleRight && holder.x + holder.width >= visibleLeft) {
                    allDanmusGone = false
                    hasVisibleDanmus = true
                    drawDanmu(canvas, holder)
                }
            }
        }

        // 只有当所有弹幕都消失，且当前索引已经到达列表末尾时，才触发播放完成
        if (allDanmusGone &&
                        !hasVisibleDanmus &&
                        currentIndex >= allDanmuList.size &&
                        allDanmuList.isNotEmpty()
        ) {
            // 通知播放完成
            danmuPlayCompleteListener?.onDanmuPlayComplete()
            // 清空弹幕列表
            allDanmuList.clear()
            currentIndex = 0
        }

        // 如果还有弹幕在显示，继续刷新
        if (!allDanmusGone || currentIndex < allDanmuList.size) {
            invalidate()
        }
    }

    private fun drawDanmu(canvas: Canvas, holder: DanmuViewHolder) {
        // 绘制背景
        paint.style = Paint.Style.FILL

        // 计算渐变的起始和结束位置
        val gradientStartX = holder.x
        val gradientEndX = holder.x + holder.width

        // 创建线性渐变
        if (holder.danmuItem.style == DanmuItem.STYLE_1) {
            paint.shader =
                    LinearGradient(
                            gradientStartX,
                            0f,
                            gradientEndX,
                            0f,
                            intArrayOf(
                                    Color.parseColor("#FF4081"),
                                    Color.parseColor("#FF6E40"),
                                    Color.parseColor("#FF4081")
                            ),
                            null,
                            Shader.TileMode.CLAMP
                    )
        } else {
            paint.shader =
                    LinearGradient(
                            gradientStartX,
                            0f,
                            gradientEndX,
                            0f,
                            intArrayOf(
                                    Color.parseColor("#2196F3"),
                                    Color.parseColor("#00BCD4"),
                                    Color.parseColor("#2196F3")
                            ),
                            null,
                            Shader.TileMode.CLAMP
                    )
        }

        val cornerRadius = holder.height / 2
        canvas.drawRoundRect(holder.rect, cornerRadius, cornerRadius, paint)

        // 只在样式1时绘制边框
        if (holder.danmuItem.style == DanmuItem.STYLE_1) {
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor(DanmuConfig.BORDER_COLOR)
            canvas.drawRoundRect(holder.rect, cornerRadius, cornerRadius, paint)
        }

        // 重置画笔样式和 shader
        paint.shader = null
        paint.style = Paint.Style.FILL
        paint.color = Color.WHITE

        // 绘制头像
        holder.avatarBitmap?.let { avatar ->
            val avatarRect =
                    RectF(
                            holder.x + paddingLeft,
                            holder.y - holder.height + (holder.height - avatarSize) / 2, // 垂直居中
                            holder.x + paddingLeft + avatarSize,
                            holder.y - (holder.height - avatarSize) / 2 // 垂直居中
                    )
            // 直接绘制圆形裁剪后的头像，不需要再次裁剪
            canvas.drawBitmap(avatar, null, avatarRect, paint)
        }

        // 绘制文本，确保在32dp高度内垂直居中
        val textX = holder.x + paddingLeft + avatarSize + paddingLeft
        val textY = holder.y - holder.height / 2 + textSize / 3

        if (holder.danmuItem.style == DanmuItem.STYLE_1) {
            // 样式1：绘制用户名
            textPaint.color = Color.parseColor(DanmuConfig.USERNAME_COLOR)
            textPaint.isFakeBoldText = true
            canvas.drawText(holder.danmuItem.username, textX, textY, textPaint)

            // 绘制内容
            val usernameWidth = textPaint.measureText(holder.danmuItem.username)
            textPaint.color = contentTextColor
            textPaint.isFakeBoldText = false
            canvas.drawText(
                    holder.danmuItem.content,
                    textX + usernameWidth + paddingLeft,
                    textY,
                    textPaint
            )
        } else {
            // 样式2：只绘制内容
            textPaint.color = contentTextColor
            textPaint.isFakeBoldText = false
            canvas.drawText(holder.danmuItem.content, textX, textY, textPaint)
        }

        // 绘制图片，确保在32dp高度内垂直居中
        holder.imageBitmap?.let { image ->
            val contentWidth =
                    if (holder.danmuItem.style == DanmuItem.STYLE_1) {
                        textPaint.measureText(holder.danmuItem.username) +
                                paddingLeft +
                                textPaint.measureText(holder.danmuItem.content)
                    } else {
                        textPaint.measureText(holder.danmuItem.content)
                    }

            val imageRect =
                    RectF(
                            textX + contentWidth + imageMarginLeft,
                            holder.y - holder.height / 2 - imageSize / 2,
                            textX + contentWidth + imageMarginLeft + imageSize,
                            holder.y - holder.height / 2 + imageSize / 2
                    )
            canvas.drawBitmap(image, null, imageRect, paint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        isAnimating = false
        isPaused = false
        pausedTime = 0
        accumulatedTime = 0
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
        isPaused = false
        pausedTime = 0
        accumulatedTime = 0
        lastFrameTime = System.nanoTime()
        currentIndex = 0
        clearCurrentDanmu()
        startDisplayingDanmu()
    }

    private fun clearCurrentDanmu() {
        danmuRows.forEach { it.clear() }
        invalidate()
    }

    private fun startDisplayingDanmu() {
        postDelayed(
                object : Runnable {
                    override fun run() {
                        if (currentIndex < allDanmuList.size) {
                            addNextColumnDanmu()
                            postDelayed(this, calculateNextDelay())
                        }
                    }
                },
                calculateNextDelay()
        )
    }

    private fun addNextColumnDanmu() {
        // 计算当前列的 x 坐标
        val columnStartX = width.toFloat()

        // 尝试在一列中添加最多3条弹幕
        var addedCount = 0
        while (addedCount < DanmuConfig.MAX_LINES && currentIndex < allDanmuList.size) {
            // 计算当前弹幕应该在的行号
            val rowIndex = addedCount

            val danmu = allDanmuList[currentIndex]

            // 计算 y 坐标
            val y = rowSpacing + (rowIndex * (danmuHeight + rowSpacing)) + danmuHeight

            // 计算弹幕宽度
            val textWidth =
                    if (danmu.style == DanmuItem.STYLE_1) {
                        textPaint.measureText(danmu.username + "  " + danmu.content)
                    } else {
                        textPaint.measureText(danmu.content)
                    }

            val totalWidth =
                    paddingLeft +
                            avatarSize +
                            paddingLeft +
                            textWidth +
                            (danmu.image?.let { imageSize + imageMarginLeft } ?: 0f) +
                            paddingRight

            // 创建 holder
            val holder =
                    DanmuViewHolder(
                            danmuItem = danmu,
                            x = columnStartX,
                            y = y,
                            width = totalWidth,
                            height = danmuHeight
                    )
            holder.updateRect()

            // 添加到对应行
            danmuRows[rowIndex].add(holder)

            // 加载图片
            loadAvatar(danmu.avatar, holder)
            danmu.image?.let { loadImage(it, holder) }

            currentIndex++
            addedCount++
        }

        invalidate()
    }

    private fun calculateNextDelay(): Long {
        return 300L // 使用固定的延迟时间，确保弹幕间距更均匀
    }

    // 添加新方法：检查弹幕是否在可视区域内
    private fun isVisible(holder: DanmuViewHolder): Boolean {
        // 创建一个向右扩展的可视区域，向右扩展一个屏幕宽度
        val extendedVisibleRect =
                RectF(
                        visibleRect.left,
                        visibleRect.top,
                        visibleRect.right + width, // 向右扩展一个屏幕宽度
                        visibleRect.bottom
                )

        // 创建弹幕的包围盒
        val danmuRect = RectF(holder.x, holder.y - holder.height, holder.x + holder.width, holder.y)

        // 检查是否与扩展的可视区域相交
        return RectF.intersects(extendedVisibleRect, danmuRect)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        visibleRect.set(0f, 0f, w.toFloat(), h.toFloat())

        // 重新计算所有弹幕的位置
        danmuRows.forEachIndexed { rowIndex, row ->
            row.forEach { holder ->
                val y = rowSpacing + (rowIndex * (danmuHeight + rowSpacing)) + danmuHeight
                holder.y = y
                holder.updateRect()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width =
                when (widthMode) {
                    MeasureSpec.EXACTLY -> widthSize
                    MeasureSpec.AT_MOST -> widthSize
                    else -> suggestedMinimumWidth
                }

        // 计算所需的高度：行数 * (弹幕高度 + 行间距) + 顶部和底部的行间距
        val desiredHeight = DanmuConfig.MAX_LINES * (danmuHeight + rowSpacing) + rowSpacing

        val height =
                when (heightMode) {
                    MeasureSpec.EXACTLY -> heightSize
                    MeasureSpec.AT_MOST -> desiredHeight.toInt().coerceAtMost(heightSize)
                    else -> desiredHeight.toInt()
                }

        setMeasuredDimension(width, height)
    }

    // 修改暂停方法
    fun pause() {
        if (!isPaused) {
            isPaused = true
            pausedTime = System.nanoTime()
            animator?.pause()
        }
    }

    // 修改恢复方法
    fun resume() {
        if (isPaused) {
            val currentTime = System.nanoTime()
            // 计算暂停的时间差，并将其加到 lastFrameTime 上
            val pauseDuration = currentTime - pausedTime
            lastFrameTime += pauseDuration

            isPaused = false
            animator?.resume()
            if (animator == null) {
                startScrollAnimation()
            }
        }
    }

    // 添加设置监听器的方法
    fun setOnNeedMoreDanmuListener(listener: () -> Unit) {
        onNeedMoreDanmuListener = listener
    }

    // 添加设置点击监听器的方法
    fun setOnDanmuClickListener(listener: (DanmuItem) -> Unit) {
        onDanmuClickListener = listener
    }

    // 在 onTouchEvent 中处理点击事件
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartX = event.x
                touchStartY = event.y

                // 查找被点击的弹幕
                touchedDanmu = findTouchedDanmu(touchStartX, touchStartY)
                return touchedDanmu != null
            }
            MotionEvent.ACTION_MOVE -> {
                // 如果移动距离超过阈值，取消点击
                if (touchedDanmu != null) {
                    val deltaX = abs(event.x - touchStartX)
                    val deltaY = abs(event.y - touchStartY)
                    if (deltaX > touchSlop || deltaY > touchSlop) {
                        touchedDanmu = null
                        return false
                    }
                    return true
                }
            }
            MotionEvent.ACTION_UP -> {
                // 如果抬起时仍然有被点击的弹幕，且位置接近，则触发点击事件
                touchedDanmu?.let {
                    val deltaX = abs(event.x - touchStartX)
                    val deltaY = abs(event.y - touchStartY)
                    if (deltaX <= touchSlop && deltaY <= touchSlop) {
                        onDanmuClickListener?.invoke(it.danmuItem)
                        performClick()
                    }
                }
                touchedDanmu = null
            }
            MotionEvent.ACTION_CANCEL -> {
                touchedDanmu = null
            }
        }
        return super.onTouchEvent(event)
    }

    // 添加 performClick 重写
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    // 查找被点击的弹幕
    private fun findTouchedDanmu(x: Float, y: Float): DanmuViewHolder? {
        for (row in danmuRows) {
            // 从后往前遍历，这样可以优先处理上层的弹幕
            for (holder in row.reversed()) {
                if (holder.rect.contains(x, y)) {
                    return holder
                }
            }
        }
        return null
    }

    // 添加设置监听器的方法
    fun setDanmuPlayCompleteListener(listener: DanmuPlayCompleteListener) {
        this.danmuPlayCompleteListener = listener
    }

    // 添加新方法：找到最适合的行
    private fun findBestRow(): Int {
        var bestRow = 0
        var maxEndX = Float.MIN_VALUE

        // 遍历每一行，找到末尾弹幕最靠前的那一行
        for (i in danmuRows.indices) {
            val row = danmuRows[i]
            val endX =
                    if (row.isEmpty()) {
                        Float.MIN_VALUE
                    } else {
                        row.last().x + row.last().width
                    }

            if (endX < maxEndX) {
                maxEndX = endX
                bestRow = i
            }
        }

        return bestRow
    }
}
