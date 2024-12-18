package com.learning.androidlearning.sample.danmu

import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.learning.androidlearning.R
import android.widget.Button
import android.util.Log
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

    private fun decodeBase64Url(base64Url: String): String {
        return try {
            String(Base64.decode(base64Url, Base64.DEFAULT))
        } catch (e: Exception) {
            Log.e("DanmuDemo", "Base64 decode failed", e)
            "" // 解码失败返回空字符串
        }
    }

    private val testDanmuList = listOf(
        // 样式1的弹幕
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "短名字",
            content = "这是一条很短的弹幕",
            image = "ic_red_packet",
            style = DanmuItem.STYLE_1
        ),
        // 样式2的弹幕
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "这是一个特别长的用户名称",
            content = "中等长度的弹幕内容示例",
            image = "ic_red_packet",
            style = DanmuItem.STYLE_2
        ),
        // ... 继续添加更多测试数据，交替使用两种样式 ...
    )+ List(45) { index ->
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "用户${index + 3}",
            content = "这是第${index + 3}条测试弹幕",
            image = "ic_red_packet",
            style = if (index % 2 == 0) DanmuItem.STYLE_1 else DanmuItem.STYLE_2
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_danmu_demo)

        danmuView = findViewById(R.id.danmuView)
        danmuInput = findViewById(R.id.danmuInput)
        sendButton = findViewById(R.id.sendButton)
        replayButton = findViewById(R.id.replayButton)
        pauseResumeButton = findViewById(R.id.pauseResumeButton)

        // 设置测试数据
        danmuView.setDanmuList(testDanmuList)

        // 发送按钮点击事件
        sendButton.setOnClickListener {
            val content = danmuInput.text?.toString()
            if (!content.isNullOrEmpty()) {
                val newDanmu = DanmuItem(
                    avatar = decodeBase64Url(url),
                    username = "用户名",
                    content = content,
                    image = "ic_red_packet",
                    style = if (Random.nextBoolean()) DanmuItem.STYLE_1 else DanmuItem.STYLE_2
                )
                danmuView.addDanmu(newDanmu)
                danmuInput.text?.clear()
            }
        }

        // 重播按钮点击事件
        replayButton.setOnClickListener {
            danmuView.replay()
            isPaused = false
            pauseResumeButton.text = "暂停"
        }

        // 暂停/恢复按钮点击事件
        pauseResumeButton.setOnClickListener {
            if (isPaused) {
                danmuView.resume()
                pauseResumeButton.text = "暂停"
            } else {
                danmuView.pause()
                pauseResumeButton.text = "继续"
            }
            isPaused = !isPaused
        }

        // 弹幕完成回调
        danmuView.setOnDanmuCompleteListener { danmu ->
            Log.d("DanmuDemo", "Danmu completed: ${danmu.content}")
        }
    }
} 