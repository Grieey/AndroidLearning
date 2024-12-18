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

    private fun decodeBase64Url(base64Url: String): String {
        return try {
            String(Base64.decode(base64Url, Base64.DEFAULT))
        } catch (e: Exception) {
            Log.e("DanmuDemo", "Base64 decode failed", e)
            "" // 解码失败返回空字符串
        }
    }

    private val testDanmuList = listOf(
        // 第一列
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "短名字",
            content = "这是一条很短的弹幕",
            image = "ic_red_packet",
            hasBorder = true
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "这是一个特别长的用户名称",
            content = "中等长度的弹幕内容示例",
            image = "ic_red_packet",
            hasBorder = false
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "普通用户",
            content = "这是一条非常非常非常非常非常非常非常非常长的弹幕内容",
            image = "ic_red_packet",
            hasBorder = true
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "用户A",
            content = "第二列第一条",
            image = "ic_red_packet",
            hasBorder = false
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "用户B",
            content = "第二列第二条",
            image = "ic_red_packet",
            hasBorder = true
        ),
        // ... 继续添加更多数据，直到50条
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "用户C",
            content = "这是一条测试弹幕",
            image = "ic_red_packet",
            hasBorder = false
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "用户D",
            content = "弹幕测试内容",
            image = "ic_red_packet",
            hasBorder = true
        ),
        // ... 继续添加剩余的测试数据，保持交替使用两种样式
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "用户49",
            content = "倒数第二条弹幕",
            image = "ic_red_packet",
            hasBorder = false
        ),
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "用户50",
            content = "最后一条弹幕",
            image = "ic_red_packet",
            hasBorder = true
        )
    ) + List(45) { index ->
        DanmuItem(
            avatar = decodeBase64Url(url),
            username = "用户${index + 6}",
            content = "这是第${index + 6}条测试弹幕",
            image = "ic_red_packet",
            hasBorder = index % 2 == 0
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_danmu_demo)

        danmuView = findViewById(R.id.danmuView)
        danmuInput = findViewById(R.id.danmuInput)
        sendButton = findViewById(R.id.sendButton)
        replayButton = findViewById(R.id.replayButton)

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
                    hasBorder = Random.nextBoolean() // 随机选择样式
                )
                danmuView.addDanmu(newDanmu)
                danmuInput.text?.clear()
            }
        }

        // 重播按钮点击事件
        replayButton.setOnClickListener {
            danmuView.replay()
        }

        // 弹幕完成回调
        danmuView.setOnDanmuCompleteListener { danmu ->
            Log.d("DanmuDemo", "Danmu completed: ${danmu.content}")
        }
    }
} 