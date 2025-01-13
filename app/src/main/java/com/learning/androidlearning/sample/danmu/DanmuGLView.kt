package com.learning.androidlearning.sample.danmu

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log

class DanmuGLView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
        GLSurfaceView(context, attrs) {

    private lateinit var renderer: DanmuGLRenderer

    init {
        initializeGLView()
    }

    private fun initializeGLView() {
        try {
            // 设置透明背景
            setZOrderOnTop(true)
            // 使用 RGBA_8888 格式
            setEGLConfigChooser(8, 8, 8, 8, 0, 0)
            holder.setFormat(android.graphics.PixelFormat.TRANSLUCENT)

            // 设置 OpenGL ES 2.0 context
            setEGLContextClientVersion(2)

            // 创建渲染器
            if (!::renderer.isInitialized) {
                renderer = DanmuGLRenderer(context, this)
                // 设置渲染器
                setRenderer(renderer)
            }

            // 设置渲染模式为持续渲染
            renderMode = RENDERMODE_CONTINUOUSLY

            // 保持屏幕常亮
            keepScreenOn = true
        } catch (e: Exception) {
            Log.e("DanmuGLView", "Failed to initialize GLView", e)
        }
    }

    private fun safeQueueEvent(event: () -> Unit) {
        try {
            if (::renderer.isInitialized && holder.surface.isValid) {
                queueEvent {
                    try {
                        event()
                    } catch (e: Exception) {
                        android.util.Log.e("DanmuGLView", "Failed to execute event", e)
                    }
                }
            } else {
                android.util.Log.w(
                        "DanmuGLView",
                        "Cannot queue event: renderer not initialized or surface invalid"
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("DanmuGLView", "Failed to queue event", e)
        }
    }

    fun setDanmuList(danmuList: List<DanmuItem>) {
        android.util.Log.d("DanmuGLView", "Setting danmu list with size: ${danmuList.size}")
        try {
            safeQueueEvent { renderer.setDanmuList(danmuList) }
        } catch (e: Exception) {
            android.util.Log.e("DanmuGLView", "Error setting danmu list", e)
        }
    }

    fun pause() {
        safeQueueEvent { renderer.pause() }
    }

    fun resume() {
        safeQueueEvent { renderer.resume() }
    }

    fun replay() {
        safeQueueEvent { renderer.replay() }
    }

    fun setOnNeedMoreDanmuListener(listener: () -> Unit) {
        safeQueueEvent { renderer.setOnNeedMoreDanmuListener(listener) }
    }

    fun setOnDanmuClickListener(listener: (DanmuItem) -> Unit) {
        safeQueueEvent { renderer.setOnDanmuClickListener(listener) }
    }

    fun setDanmuPlayCompleteListener(listener: DanmuPlayCompleteListener) {
        safeQueueEvent { renderer.setDanmuPlayCompleteListener(listener) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        safeQueueEvent { renderer.clearCurrentDanmu() }
    }

    override fun onPause() {
        super.onPause()
        pause()
    }

    override fun onResume() {
        super.onResume()
        resume()
    }
}
