package com.learning.androidlearning.sample.danmu

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.learning.androidlearning.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class DanmuGLRenderer(private val context: Context, private val glSurfaceView: GLSurfaceView) :
        GLSurfaceView.Renderer {
    private var programId = 0
    private var positionHandle = 0
    private var texCoordHandle = 0
    private var mvpMatrixHandle = 0
    private var textureHandle = 0

    private val mvpMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelMatrix = FloatArray(16)

    private val vertexBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(12 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    private val textureBuffer: FloatBuffer =
            ByteBuffer.allocateDirect(8 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()

    private val danmuRows = Array(DanmuConfig.MAX_LINES) { mutableListOf<DanmuGLViewHolder>() }
    private var danmuList = mutableListOf<DanmuItem>()
    private var currentIndex = 0
    private var isPaused = false
    private var lastFrameTime = System.nanoTime()
    private var onNeedMoreDanmuListener: (() -> Unit)? = null
    private var onDanmuClickListener: ((DanmuItem) -> Unit)? = null
    private var danmuPlayCompleteListener: DanmuPlayCompleteListener? = null

    private val textPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize =
                        context.resources.getDimensionPixelSize(R.dimen.danmu_text_size).toFloat()
                color = Color.WHITE
            }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textSize =
            context.resources.getDimensionPixelSize(R.dimen.danmu_text_size).toFloat()

    private val danmuHeight =
            context.resources.getDimensionPixelSize(R.dimen.danmu_height).toFloat()
    private val avatarSize =
            context.resources.getDimensionPixelSize(R.dimen.danmu_avatar_size).toFloat()
    private val imageSize =
            context.resources.getDimensionPixelSize(R.dimen.danmu_image_size).toFloat()
    private val paddingLeft =
            context.resources.getDimensionPixelSize(R.dimen.danmu_padding_left).toFloat()
    private val paddingRight =
            context.resources.getDimensionPixelSize(R.dimen.danmu_padding_right).toFloat()
    private val imageMarginLeft =
            context.resources.getDimensionPixelSize(R.dimen.danmu_image_margin_left).toFloat()
    private val rowSpacing =
            context.resources.getDimensionPixelSize(R.dimen.danmu_row_spacing).toFloat()
    private val safeDistance =
            context.resources.getDimensionPixelSize(R.dimen.danmu_safe_distance).toFloat()

    private var screenWidth = 0
    private var screenHeight = 0
    private val scrollSpeed = 200f // 每秒移动的像素数

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // 编译着色器程序
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)

        // 创建OpenGL程序
        programId =
                GLES20.glCreateProgram().also { program ->
                    GLES20.glAttachShader(program, vertexShader)
                    GLES20.glAttachShader(program, fragmentShader)
                    GLES20.glLinkProgram(program)

                    // 检查链接状态
                    val linkStatus = IntArray(1)
                    GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
                    if (linkStatus[0] != GLES20.GL_TRUE) {
                        val error = GLES20.glGetProgramInfoLog(program)
                        throw RuntimeException("Program linking failed: $error")
                    }
                }

        // 获取着色器中的变量句柄
        positionHandle = GLES20.glGetAttribLocation(programId, "vPosition")
        texCoordHandle = GLES20.glGetAttribLocation(programId, "aTexCoord")
        mvpMatrixHandle = GLES20.glGetUniformLocation(programId, "uMVPMatrix")
        textureHandle = GLES20.glGetUniformLocation(programId, "sTexture")

        // 检查句柄是否有效
        if (positionHandle == -1 ||
                        texCoordHandle == -1 ||
                        mvpMatrixHandle == -1 ||
                        textureHandle == -1
        ) {
            throw RuntimeException("Failed to get shader handles")
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        screenWidth = width
        screenHeight = height

        // 设置正交投影矩阵，使用屏幕坐标系
        Matrix.orthoM(projectionMatrix, 0, 0f, width.toFloat(), height.toFloat(), 0f, -1f, 1f)

        // 设置视图矩阵为单位矩阵
        Matrix.setIdentityM(viewMatrix, 0)

        // 计算最终的MVP矩阵
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        if (isPaused) return

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 使用着色器程序
        GLES20.glUseProgram(programId)

        // 更新弹幕位置
        updateDanmuPositions()

        // 绘制每个弹幕
        drawDanmus()

        // 检查是否需要添加新弹幕
        checkAndAddNewDanmu()
    }

    private fun updateDanmuPositions() {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f
        lastFrameTime = currentTime

        val distance = scrollSpeed * deltaTime

        var needsRedraw = false
        var hasVisibleDanmu = false
        var maxX = Float.MIN_VALUE

        for (row in danmuRows.indices) {
            val currentRow = danmuRows[row]
            val iterator = currentRow.iterator()
            var prevHolder: DanmuGLViewHolder? = null

            while (iterator.hasNext()) {
                val holder = iterator.next()

                // 移除已经完全离开屏幕的弹幕
                if (holder.x + holder.width < 0) {
                    iterator.remove()
                    holder.release()
                    continue
                }

                val oldX = holder.x
                holder.updatePosition(-distance)

                // 检查与前一个弹幕的间距
                prevHolder?.let { prev ->
                    val currentDistance = holder.x - (prev.x + prev.width)
                    if (currentDistance < safeDistance) {
                        holder.x = prev.x + prev.width + safeDistance
                        holder.updateRect()
                        holder.updateVertices(holder.x, holder.y, holder.width, holder.height)
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

        if (hasVisibleDanmu && maxX < screenWidth * 1.5f) {
            onNeedMoreDanmuListener?.invoke()
        }
    }

    private fun drawDanmus() {
        // 设置MVP矩阵
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // 启用混合
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // 遍历每一行弹幕
        danmuRows.forEach { row -> row.forEach { holder -> drawDanmu(holder) } }
    }

    private fun drawDanmu(holder: DanmuGLViewHolder) {
        // 启用混合
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // 设置MVP矩阵
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // 设置纹理坐标缓冲区
        textureBuffer.clear()
        textureBuffer.put(holder.textureCoords)
        textureBuffer.position(0)

        // 设置纹理坐标
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        // 绘制背景
        if (holder.backgroundTextureId != -1) {
            vertexBuffer.clear()
            vertexBuffer.put(holder.backgroundVertices)
            vertexBuffer.position(0)

            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
            GLES20.glEnableVertexAttribArray(positionHandle)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, holder.backgroundTextureId)
            GLES20.glUniform1i(textureHandle, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }

        // 绘制头像
        if (holder.avatarTextureId != -1) {
            vertexBuffer.clear()
            vertexBuffer.put(holder.avatarVertices)
            vertexBuffer.position(0)

            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
            GLES20.glEnableVertexAttribArray(positionHandle)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, holder.avatarTextureId)
            GLES20.glUniform1i(textureHandle, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }

        // 绘制图片
        if (holder.imageTextureId != -1) {
            vertexBuffer.clear()
            vertexBuffer.put(holder.imageVertices)
            vertexBuffer.position(0)

            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
            GLES20.glEnableVertexAttribArray(positionHandle)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, holder.imageTextureId)
            GLES20.glUniform1i(textureHandle, 0)
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        }

        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun checkAndAddNewDanmu() {
        if (currentIndex < danmuList.size) {
            val rowIndex = findBestRow()
            android.util.Log.d(
                    "DanmuGLRenderer",
                    "Found best row: $rowIndex for danmu index: $currentIndex"
            )
            if (rowIndex != -1) {
                addDanmuToRow(danmuList[currentIndex], rowIndex)
                currentIndex++
            }
        }
    }

    private fun findBestRow(): Int {
        var bestRow = -1
        var maxEndX = Float.MIN_VALUE

        for (i in danmuRows.indices) {
            val row = danmuRows[i]
            val endX = if (row.isEmpty()) Float.MIN_VALUE else row.last().x + row.last().width

            if (endX < screenWidth) {
                if (bestRow == -1 || endX > maxEndX) {
                    bestRow = i
                    maxEndX = endX
                }
            }
        }

        return bestRow
    }

    private fun addDanmuToRow(danmu: DanmuItem, rowIndex: Int) {
        android.util.Log.d("DanmuGLRenderer", "Adding danmu to row $rowIndex: ${danmu.content}")
        val y = rowSpacing + (rowIndex * (danmuHeight + rowSpacing))

        // 计算文本宽度
        val textWidth =
                if (danmu.style == DanmuItem.STYLE_1) {
                    textPaint.measureText(danmu.username + "  " + danmu.content)
                } else {
                    textPaint.measureText(danmu.content)
                }

        // 计算总宽度
        val totalWidth =
                paddingLeft + // 左边距
                avatarSize + // 头像宽度
                        paddingLeft + // 头像和文本之间的间距
                        textWidth + // 文本宽度
                        (if (danmu.image != null) {
                            imageMarginLeft + imageSize + paddingRight // 图片相关的宽度
                        } else {
                            paddingRight // 没有图片时的右边距
                        })

        // 计算起始 x 坐标（从屏幕右侧开始）
        var startX = screenWidth.toFloat()

        // 如果该行已有弹幕，确保新弹幕与最后一个弹幕保持安全距离
        if (danmuRows[rowIndex].isNotEmpty()) {
            val lastDanmu = danmuRows[rowIndex].last()
            startX = maxOf(startX, lastDanmu.x + lastDanmu.width + safeDistance)
        }

        val holder =
                DanmuGLViewHolder(
                        danmu,
                        startX,
                        y,
                        totalWidth,
                        danmuHeight,
                        textWidth,
                        paddingLeft,
                        avatarSize,
                        imageSize,
                        imageMarginLeft
                )
        holder.updateRect()

        // 更新背景顶点坐标
        holder.updateVertices(startX, y, totalWidth, danmuHeight)

        // 更新头像顶点坐标（垂直居中）
        val avatarX = startX + paddingLeft
        val avatarY = y + (danmuHeight - avatarSize) / 2
        // 使用实际的物理像素大小
        val actualAvatarSize = avatarSize * context.resources.displayMetrics.density
        holder.updateAvatarVertices(avatarX, avatarY, actualAvatarSize)

        // 更新文本区域顶点坐标
        val textX = avatarX + avatarSize + paddingLeft
        holder.updateTextVertices(textX, y, textWidth, danmuHeight)

        // 如果有图片，更新图片顶点坐标（在文本后面，垂直居中）
        if (danmu.image != null) {
            android.util.Log.d("DanmuGLRenderer", "Setting up image for danmu: ${danmu.content}")
            val imageX = textX + textWidth * 2 + imageMarginLeft
            val imageY = y + (danmuHeight - imageSize) / 2
            // 使用实际的物理像素大小
            val actualImageSize = imageSize * context.resources.displayMetrics.density
            holder.updateImageVertices(imageX, imageY, actualImageSize)
        }

        // 创建背景纹理
        createBackgroundTexture(holder)

        // 加载头像
        loadAvatar(danmu.avatar, holder)

        // 加载图片（如果有）
        danmu.image?.let { loadImage(it, holder) }

        danmuRows[rowIndex].add(holder)
    }

    private fun createBackgroundTexture(holder: DanmuGLViewHolder) {
        val bitmap =
                Bitmap.createBitmap(
                        holder.width.toInt(),
                        holder.height.toInt(),
                        Bitmap.Config.ARGB_8888
                )
        val canvas = Canvas(bitmap)

        // 创建渐变背景
        val shader =
                if (holder.danmuItem.style == DanmuItem.STYLE_1) {
                    LinearGradient(
                            0f,
                            0f,
                            holder.width,
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
                    LinearGradient(
                            0f,
                            0f,
                            holder.width,
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

        paint.shader = shader
        canvas.drawRoundRect(
                0f,
                0f,
                holder.width,
                holder.height,
                holder.height / 2,
                holder.height / 2,
                paint
        )

        // 绘制文本
        paint.shader = null
        paint.color = Color.WHITE
        paint.textSize = textSize
        paint.textAlign = Paint.Align.LEFT

        // 计算文本基线位置
        val fontMetrics = paint.fontMetrics
        val textY = (holder.height - fontMetrics.top - fontMetrics.bottom) / 2

        // 计算文本起始位置
        val textStartX = paddingLeft + avatarSize + paddingLeft

        if (holder.danmuItem.style == DanmuItem.STYLE_1) {
            // 绘制用户名
            paint.isFakeBoldText = true
            canvas.drawText(holder.danmuItem.username, textStartX, textY, paint)

            // 绘制内容
            val usernameWidth = paint.measureText(holder.danmuItem.username)
            paint.isFakeBoldText = false
            canvas.drawText(
                    holder.danmuItem.content,
                    textStartX + usernameWidth + paddingLeft,
                    textY,
                    paint
            )
        } else {
            // 只绘制内容
            paint.isFakeBoldText = false
            canvas.drawText(holder.danmuItem.content, textStartX, textY, paint)
        }

        // 创建纹理
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        holder.backgroundTextureId = textureIds[0]

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, holder.backgroundTextureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        bitmap.recycle()
    }

    private fun loadAvatar(url: String, holder: DanmuGLViewHolder) {
        // 计算实际的头像大小
        val actualAvatarSize = (avatarSize * context.resources.displayMetrics.density).toInt()
        android.util.Log.d("DanmuGLRenderer", "Loading avatar with size: $actualAvatarSize")

        Glide.with(context)
                .asBitmap()
                .load(url)
                .override(actualAvatarSize, actualAvatarSize)
                .into(
                        object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                            ) {
                                android.util.Log.d(
                                        "DanmuGLRenderer",
                                        "Avatar loaded with size: ${resource.width}x${resource.height}"
                                )
                                // 创建圆形头像
                                val circularBitmap = createCircularBitmap(resource)
                                holder.avatarBitmap = circularBitmap

                                // 创建纹理
                                glSurfaceView.queueEvent {
                                    try {
                                        val textureIds = IntArray(1)
                                        GLES20.glGenTextures(1, textureIds, 0)
                                        holder.avatarTextureId = textureIds[0]

                                        GLES20.glBindTexture(
                                                GLES20.GL_TEXTURE_2D,
                                                holder.avatarTextureId
                                        )
                                        GLES20.glTexParameteri(
                                                GLES20.GL_TEXTURE_2D,
                                                GLES20.GL_TEXTURE_MIN_FILTER,
                                                GLES20.GL_LINEAR
                                        )
                                        GLES20.glTexParameteri(
                                                GLES20.GL_TEXTURE_2D,
                                                GLES20.GL_TEXTURE_MAG_FILTER,
                                                GLES20.GL_LINEAR
                                        )
                                        GLES20.glTexParameteri(
                                                GLES20.GL_TEXTURE_2D,
                                                GLES20.GL_TEXTURE_WRAP_S,
                                                GLES20.GL_CLAMP_TO_EDGE
                                        )
                                        GLES20.glTexParameteri(
                                                GLES20.GL_TEXTURE_2D,
                                                GLES20.GL_TEXTURE_WRAP_T,
                                                GLES20.GL_CLAMP_TO_EDGE
                                        )
                                        GLUtils.texImage2D(
                                                GLES20.GL_TEXTURE_2D,
                                                0,
                                                circularBitmap,
                                                0
                                        )

                                        // 只有在成功上传到 GPU 后才回收 bitmap
                                        circularBitmap.recycle()
                                    } catch (e: Exception) {
                                        android.util.Log.e(
                                                "DanmuGLRenderer",
                                                "Error creating avatar texture",
                                                e
                                        )
                                        // 如果出错，确保回收 bitmap
                                        if (!circularBitmap.isRecycled) {
                                            circularBitmap.recycle()
                                        }
                                    }
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                holder.avatarBitmap?.recycle()
                                holder.avatarBitmap = null
                                if (holder.avatarTextureId != -1) {
                                    GLES20.glDeleteTextures(
                                            1,
                                            intArrayOf(holder.avatarTextureId),
                                            0
                                    )
                                    holder.avatarTextureId = -1
                                }
                            }
                        }
                )
    }

    private fun loadImage(url: String, holder: DanmuGLViewHolder) {
        android.util.Log.d("DanmuGLRenderer", "Loading image from url: $url")
        // 对于本地图片资源，直接从资源加载
        if (url.startsWith("ic_")) {
            val resourceId = context.resources.getIdentifier(url, "drawable", context.packageName)
            if (resourceId != 0) {
                android.util.Log.d(
                        "DanmuGLRenderer",
                        "Found resource id: $resourceId for url: $url"
                )
                Glide.with(context)
                        .asBitmap()
                        .load(resourceId)
                        .override(imageSize.toInt(), imageSize.toInt())
                        .into(
                                object : CustomTarget<Bitmap>() {
                                    override fun onResourceReady(
                                            resource: Bitmap,
                                            transition: Transition<in Bitmap>?
                                    ) {
                                        android.util.Log.d(
                                                "DanmuGLRenderer",
                                                "Image loaded successfully, size: ${resource.width}x${resource.height}"
                                        )
                                        holder.imageBitmap = resource
                                        glSurfaceView.queueEvent {
                                            createImageTexture(resource, holder)
                                        }
                                    }

                                    override fun onLoadCleared(placeholder: Drawable?) {
                                        holder.imageBitmap?.recycle()
                                        holder.imageBitmap = null
                                        if (holder.imageTextureId != -1) {
                                            GLES20.glDeleteTextures(
                                                    1,
                                                    intArrayOf(holder.imageTextureId),
                                                    0
                                            )
                                            holder.imageTextureId = -1
                                        }
                                    }
                                }
                        )
            } else {
                android.util.Log.e("DanmuGLRenderer", "Resource not found: $url")
            }
            return
        }
    }

    private fun createImageTexture(bitmap: Bitmap, holder: DanmuGLViewHolder) {
        android.util.Log.d("DanmuGLRenderer", "Creating image texture")
        try {
            // 创建纹理
            val textureIds = IntArray(1)
            GLES20.glGenTextures(1, textureIds, 0)
            holder.imageTextureId = textureIds[0]

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, holder.imageTextureId)
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE
            )
            GLES20.glTexParameteri(
                    GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE
            )
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            android.util.Log.d(
                    "DanmuGLRenderer",
                    "Image texture created successfully with id: ${holder.imageTextureId}"
            )
        } catch (e: Exception) {
            android.util.Log.e("DanmuGLRenderer", "Error creating image texture", e)
        }
    }

    private fun createRoundedBitmap(source: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(0f, 0f, source.width.toFloat(), source.height.toFloat())
        val radius = minOf(source.width, source.height) / 8f // 圆角半径为尺寸的1/8

        // 绘制圆角矩形
        paint.color = Color.WHITE
        canvas.drawRoundRect(rect, radius, radius, paint)

        // 设置混合模式
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // 绘制原图
        canvas.drawBitmap(source, 0f, 0f, paint)

        return output
    }

    private fun createCircularBitmap(source: Bitmap): Bitmap {
        // 使用最小边长作为输出大小，确保不会拉伸
        val size = minOf(source.width, source.height)
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 绘制圆形遮罩
        paint.color = Color.WHITE
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)

        // 设置混合模式
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        // 计算源图片的裁剪区域，确保从中心裁剪
        val srcRect =
                if (source.width != source.height) {
                    if (source.width > source.height) {
                        // 如果宽度大于高度，从中心裁剪
                        val startX = (source.width - size) / 2
                        Rect(startX, 0, startX + size, size)
                    } else {
                        // 如果高度大于宽度，从中心裁剪
                        val startY = (source.height - size) / 2
                        Rect(0, startY, size, startY + size)
                    }
                } else {
                    // 如果是正方形，直接使用整个图片
                    Rect(0, 0, size, size)
                }

        // 目标区域为整个输出位图
        val destRect = Rect(0, 0, size, size)
        canvas.drawBitmap(source, srcRect, destRect, paint)

        return output
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)

            // 检查编译状态
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                val error = GLES20.glGetShaderInfoLog(shader)
                GLES20.glDeleteShader(shader)
                throw RuntimeException("Shader compilation failed: $error")
            }
        }
    }

    fun setDanmuList(list: List<DanmuItem>) {
        android.util.Log.d("DanmuGLRenderer", "Setting danmu list with size: ${list.size}")
        danmuList.clear()
        danmuList.addAll(list)
        currentIndex = 0
        clearCurrentDanmu()
    }

    fun clearCurrentDanmu() {
        danmuRows.forEach { row ->
            row.forEach { holder -> holder.release() }
            row.clear()
        }
    }

    fun pause() {
        isPaused = true
    }

    fun resume() {
        isPaused = false
        lastFrameTime = System.nanoTime()
    }

    fun replay() {
        currentIndex = 0
        isPaused = false
        lastFrameTime = System.nanoTime()
        clearCurrentDanmu()
    }

    fun setOnNeedMoreDanmuListener(listener: () -> Unit) {
        onNeedMoreDanmuListener = listener
    }

    fun setOnDanmuClickListener(listener: (DanmuItem) -> Unit) {
        onDanmuClickListener = listener
    }

    fun setDanmuPlayCompleteListener(listener: DanmuPlayCompleteListener) {
        danmuPlayCompleteListener = listener
    }

    companion object {
        private const val VERTEX_SHADER =
                """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vTexCoord = aTexCoord;
            }
        """

        private const val FRAGMENT_SHADER =
                """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D sTexture;
            
            void main() {
                vec4 color = texture2D(sTexture, vTexCoord);
                gl_FragColor = color;
            }
        """
    }
}
