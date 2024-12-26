package com.learning.androidlearning.sample.danmu

import android.graphics.Rect
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.textfield.TextInputEditText
import com.learning.androidlearning.R
import kotlin.random.Random

class DanmuDemoActivity : AppCompatActivity() {
    private lateinit var danmuView: DanmuView
    private lateinit var danmuInput: TextInputEditText
    private lateinit var sendButton: Button
    private lateinit var replayButton: Button
    private lateinit var pauseResumeButton: Button
    private var isPaused = false
    private var isUserPaused = false
    private lateinit var statusText: TextView

    private var currentDanmuIndex = 0
    private var currentBatchNumber = 1

    companion object {
        private const val BASE_URL =
                "aHR0cHM6Ly9hbGlpbWcuY2hhbmdiYS5jb20vY2FjaGUvcGhvdG8vOTc2NTMyODc5XzIwMF8yMDAuanBn"
    }

    private fun decodeBase64Url(base64Url: String): String {
        return try {
            String(Base64.decode(base64Url, Base64.DEFAULT))
        } catch (e: Exception) {
            Log.e("DanmuDemo", "Base64 decode failed", e)
            ""
        }
    }

    private fun generateDanmuList(count: Int, startIndex: Int = 0): List<DanmuItem> {
        return List(count) { index ->
            DanmuItem(
                    avatar = decodeBase64Url(BASE_URL),
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

        initViews()
        setupDanmuView()
        setupListeners()
    }

    private fun initViews() {
        danmuView = findViewById(R.id.danmuView)
        danmuInput = findViewById(R.id.danmuInput)
        sendButton = findViewById(R.id.sendButton)
        replayButton = findViewById(R.id.replayButton)
        pauseResumeButton = findViewById(R.id.pauseResumeButton)
        statusText = findViewById(R.id.statusText)
    }

    private fun setupDanmuView() {
        danmuView.setDanmuList(generateDanmuList(30))
        currentDanmuIndex = 30

        danmuView.setOnNeedMoreDanmuListener {
            currentBatchNumber++
            if (currentBatchNumber > 2) {
                return@setOnNeedMoreDanmuListener
            }
            val newDanmuList = generateDanmuList(30, currentDanmuIndex)
            currentDanmuIndex += 30
            newDanmuList.forEach { danmu -> danmuView.addDanmu(danmu) }
        }

        danmuView.setOnDanmuCompleteListener { danmu ->
            Log.d("DanmuDemo", "Danmu completed: ${danmu.content}")
        }

        danmuView.setOnDanmuClickListener { danmu ->
            Toast.makeText(this, "点击了弹幕: ${danmu.content}", Toast.LENGTH_SHORT).show()
        }

        danmuView.setDanmuPlayCompleteListener(
                object : DanmuPlayCompleteListener {
                    override fun onDanmuPlayComplete() {
                        runOnUiThread {
                            Toast.makeText(this@DanmuDemoActivity, "弹幕播放完成", Toast.LENGTH_SHORT)
                                    .show()
                        }
                    }
                }
        )
    }

    private fun setupListeners() {
        setupScrollListeners()
        setupButtonListeners()
    }

    private fun setupScrollListeners() {
        danmuView.post { updateDanmuVisibility() }

        danmuView.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        updateDanmuVisibility()
                    }
                }
        )

        findViewById<NestedScrollView>(R.id.scrollView)
                .setOnScrollChangeListener(
                        NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
                            updateDanmuVisibility()
                        }
                )
    }

    private fun setupButtonListeners() {
        sendButton.setOnClickListener {
            val content = danmuInput.text?.toString()
            if (!content.isNullOrEmpty()) {
                val newDanmu =
                        DanmuItem(
                                avatar = decodeBase64Url(BASE_URL),
                                username = "用户名",
                                content = "[批次${currentBatchNumber}]$content",
                                image = "ic_red_packet",
                                style =
                                        if (Random.nextBoolean()) DanmuItem.STYLE_1
                                        else DanmuItem.STYLE_2
                        )
                danmuView.addDanmu(newDanmu)
                danmuInput.text?.clear()
            }
        }

        replayButton.setOnClickListener {
            isUserPaused = false
            isPaused = false
            replay()
            pauseResumeButton.text = "暂停"
            updateStatusText()
            updateDanmuVisibility()
        }

        pauseResumeButton.setOnClickListener {
            isUserPaused = !isUserPaused
            if (isUserPaused) {
                pauseDanmu()
            } else {
                resumeDanmu()
            }
            updateStatusText()
        }
    }

    private fun updateDanmuVisibility() {
        if (!isUserPaused) {
            val danmuRect = Rect()
            val parentRect = Rect()

            danmuView.getGlobalVisibleRect(danmuRect)
            findViewById<NestedScrollView>(R.id.scrollView).getGlobalVisibleRect(parentRect)

            val isVisible =
                    danmuRect.top >= parentRect.top &&
                            danmuRect.bottom <= parentRect.bottom &&
                            danmuRect.height() > 0

            if (!isVisible && !isPaused) {
                pauseDanmu()
            } else if (isVisible && isPaused) {
                resumeDanmu()
            }
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
        val status =
                when {
                    isUserPaused -> "状态：用户暂停"
                    isPaused -> "状态：自动暂停"
                    else -> "状态：播放中"
                }
        statusText.text = status
    }

    private fun replay() {
        currentDanmuIndex = 30
        currentBatchNumber = 1
        danmuView.setDanmuList(generateDanmuList(30))
    }
}
