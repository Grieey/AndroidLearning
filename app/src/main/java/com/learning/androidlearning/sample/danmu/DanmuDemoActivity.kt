package com.learning.androidlearning.sample.danmu

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.ViewTreeObserver
import android.graphics.Rect
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.textfield.TextInputEditText
import com.learning.androidlearning.R
import kotlin.random.Random

// Base64 编码的 URL: "aHR0cHM6Ly9hbGlpbWcuY2hhbmdiYS5jb20vY2FjaGUvcGhvdG8vOTc2NTMyODc5XzIwMF8yMDAuanBn"
private val url = "aHR0cHM6Ly9hbGlpbWcuY2hhbmdiYS5jb20vY2FjaGUvcGhvdG8vOTc2NTMyODc5XzIwMF8yMDAuanBn"

class DanmuDemoActivity : AppCompatActivity() {
    private lateinit var danmuView: DanmuView
    private lateinit var danmuInput: TextInputEditText
    private lateinit var sendButton: Button
    private lateinit var replayButton: Button
    private lateinit var pauseResumeButton: Button
    private var isPaused = false
    private var isUserPaused = false // 用户手动暂停的状态
    private lateinit var statusText: TextView

    private var currentDanmuIndex = 0
    private var currentBatchNumber = 1  // 添加批次号计数

    private fun decodeBase64Url(base64Url: String): String {
        return try {
            String(Base64.decode(base64Url, Base64.DEFAULT))
        } catch (e: Exception) {
            Log.e("DanmuDemo", "Base64 decode failed", e)
            "" // 解码失败返回空字符串
        }
    }

    private fun generateDanmuList(count: Int, startIndex: Int = 0): List<DanmuItem> {
        return List(count) { index ->
            DanmuItem(
                avatar = decodeBase64Url(url),
                username = "用户${startIndex + index}",
                content = "[批次${currentBatchNumber}]这是第${startIndex + index}条测试弹幕",
                image = "ic_red_packet",
                style = if (index % 2 == 0) DanmuItem.STYLE_1 else DanmuItem.STYLE_2
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_danmu_demo)

        danmuView = findViewById(R.id.danmuView)
        danmuInput = findViewById(R.id.danmuInput)
        sendButton = findViewById(R.id.sendButton)
        replayButton = findViewById(R.id.replayButton)
        pauseResumeButton = findViewById(R.id.pauseResumeButton)
        statusText = findViewById(R.id.statusText)

        // 设置测试数据
        danmuView.setDanmuList(generateDanmuList(30))
        currentDanmuIndex = 30

        // 设置需要更多弹幕的监听
        danmuView.setOnNeedMoreDanmuListener {
            currentBatchNumber++ // 增加批次号
            // 添加新的30条弹幕
            val newDanmuList = generateDanmuList(30, currentDanmuIndex)
            currentDanmuIndex += 30
            
            // 将新弹幕添加到现有列表中
            newDanmuList.forEach { danmu ->
                danmuView.addDanmu(danmu)
            }
        }

        // 初始可见性检查
        danmuView.post {
            updateDanmuVisibility()
        }

        // 修改可见性监听的实现
        danmuView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                updateDanmuVisibility()
            }
        })

        // 修改滚动监听的实现
        findViewById<NestedScrollView>(R.id.scrollView).setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, scrollX, scrollY, oldScrollX, oldScrollY ->
                updateDanmuVisibility()
            }
        )

        // 发送按钮点击事件
        sendButton.setOnClickListener {
            val content = danmuInput.text?.toString()
            if (!content.isNullOrEmpty()) {
                val newDanmu = DanmuItem(
                    avatar = decodeBase64Url(url),
                    username = "用户名",
                    content = "[批次${currentBatchNumber}]$content",
                    image = "ic_red_packet",
                    style = if (Random.nextBoolean()) DanmuItem.STYLE_1 else DanmuItem.STYLE_2
                )
                danmuView.addDanmu(newDanmu)
                danmuInput.text?.clear()
            }
        }

        // 重播按钮点击事件
        replayButton.setOnClickListener {
            isUserPaused = false
            isPaused = false
            replay()
            pauseResumeButton.text = "暂停"
            updateStatusText()
            updateDanmuVisibility()
        }

        // 暂停/恢复按钮点击事件
        pauseResumeButton.setOnClickListener {
            isUserPaused = !isUserPaused
            if (isUserPaused) {
                pauseDanmu()
            } else {
                resumeDanmu()
            }
            updateStatusText()
        }

        // 弹幕完成回调
        danmuView.setOnDanmuCompleteListener { danmu ->
            Log.d("DanmuDemo", "Danmu completed: ${danmu.content}")
        }

        // 设置弹幕点击监听
        danmuView.setOnDanmuClickListener { danmu ->
            Toast.makeText(
                this,
                "点击了弹幕: ${danmu.content}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateDanmuVisibility() {
        if (!isUserPaused) {
            val danmuRect = Rect()
            val parentRect = Rect()
            
            // 获取弹幕视图和父视图的可见区域
            danmuView.getGlobalVisibleRect(danmuRect)
            findViewById<NestedScrollView>(R.id.scrollView).getGlobalVisibleRect(parentRect)
            
            // 计算弹幕视图在父视图中的可见部分
            val isVisible = danmuRect.top >= parentRect.top && 
                           danmuRect.bottom <= parentRect.bottom && 
                           danmuRect.height() > 0
            
            if (!isVisible && !isPaused) {
                pauseDanmu()
            } else if (isVisible && isPaused) {
                resumeDanmu()
            }
            
            Log.d("DanmuVisibility", """
                DanmuRect: top=${danmuRect.top}, bottom=${danmuRect.bottom}, height=${danmuRect.height()}
                ParentRect: top=${parentRect.top}, bottom=${parentRect.bottom}
                IsVisible: $isVisible
                IsPaused: $isPaused
            """.trimIndent())
        }
    }

    private fun pauseDanmu() {
        isPaused = true
        danmuView.pause()
        pauseResumeButton.text = "继续"
        updateStatusText()
    }

    private fun resumeDanmu() {
        isPaused = false
        danmuView.resume()
        pauseResumeButton.text = "暂停"
        updateStatusText()
    }

    private fun updateStatusText() {
        val status = when {
            isUserPaused -> "状态：用户暂停"
            isPaused -> "状态：自动暂停"
            else -> "状态：播放中"
        }
        statusText.text = status
    }

    // 修改重播逻辑
    private fun replay() {
        currentDanmuIndex = 30
        currentBatchNumber = 1  // 重置批次号
        danmuView.setDanmuList(generateDanmuList(30))
    }
} 