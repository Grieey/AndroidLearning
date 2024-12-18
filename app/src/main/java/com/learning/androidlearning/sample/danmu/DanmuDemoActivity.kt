package com.learning.androidlearning.sample.danmu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.learning.androidlearning.R
import android.widget.Button
import android.util.Log

class DanmuDemoActivity : AppCompatActivity() {
    private lateinit var danmuView: DanmuView
    private lateinit var danmuInput: TextInputEditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_danmu_demo)

        danmuView = findViewById(R.id.danmuView)
        danmuInput = findViewById(R.id.danmuInput)
        sendButton = findViewById(R.id.sendButton)

        // 示例数据
        danmuView.postDelayed({
            val sampleDanmu = DanmuItem(
                avatar = "https://pic.616pic.com/ys_img/00/05/33/i5SqXdDM9Q.jpg",
                username = "用户名",
                content = "这是一条示例弹幕",
                image = "android.resource://${packageName}/${R.drawable.default_image}"
            )

            danmuView.addDanmu(sampleDanmu)
        }, 3000)

        sendButton.setOnClickListener {
            val content = danmuInput.text?.toString()
            if (!content.isNullOrEmpty()) {
                val newDanmu = DanmuItem(
                    avatar = "https://pic.616pic.com/ys_img/00/05/33/i5SqXdDM9Q.jpg",
                    username = "用户名",
                    content = content
                )
                danmuView.addDanmu(newDanmu)
                danmuInput.text?.clear()
            }
        }

        danmuView.setOnDanmuCompleteListener { danmu ->
            // 处理弹幕播放完成的回调
            Log.d("DanmuDemo", "Danmu completed: ${danmu.content}")
        }
    }
} 