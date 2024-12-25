package com.learning.androidlearning.sample.flipclock

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.learning.androidlearning.R
import java.util.Calendar
import java.util.Timer
import java.util.TimerTask

class FlipClockDemoActivity : AppCompatActivity() {
    private var isLandscape = true
    private var isAnimating = false
    private var bit_hour: FlipLayout? = null
    private var bit_minute: FlipLayout? = null
    private var bit_second: FlipLayout? = null
    private var oldNumber: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 恢复保存的状态
        savedInstanceState?.let {
            isLandscape = it.getBoolean("isLandscape", true)
        }

        // 设置屏幕方向
        requestedOrientation = if (isLandscape) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        setContentView(R.layout.activity_flip_clock_demo)

        val rotateButton = findViewById<ImageButton>(R.id.btnToggleOrientation)
        rotateButton.setOnClickListener {
            if (isAnimating) return@setOnClickListener

            isAnimating = true
            val rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_button)
            rotateAnim.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
                override fun onAnimationStart(animation: android.view.animation.Animation?) {}

                override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                    isAnimating = false
                }

                override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            })

            rotateButton.startAnimation(rotateAnim)

            isLandscape = !isLandscape
            requestedOrientation = if (isLandscape) {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        this.bit_second = findViewById(R.id.bit_flip_3)
        this.bit_minute = findViewById(R.id.bit_flip_2)
        this.bit_hour = findViewById(R.id.bit_flip_1)

        bit_hour!!.flip(oldNumber.get(Calendar.HOUR_OF_DAY), 24, "HOUR")
        bit_minute!!.flip(oldNumber.get(Calendar.MINUTE), 60, "MINUTE")
        bit_second!!.flip(oldNumber.get(Calendar.SECOND), 60, "SECOND")

        Timer().schedule(object : TimerTask() {
            override fun run() {
                start()
            }
        }, 1000, 1000) //每一秒执行一次
    }

    // 保存状态
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isLandscape", isLandscape)
    }

    fun start() {
        val now = Calendar.getInstance()
        val nhour = now[Calendar.HOUR_OF_DAY]
        val nminute = now[Calendar.MINUTE]
        val nsecond = now[Calendar.SECOND]

        val ohour = oldNumber[Calendar.HOUR_OF_DAY]
        val ominute = oldNumber[Calendar.MINUTE]
        val osecond = oldNumber[Calendar.SECOND]

        oldNumber = now

        val hour = nhour - ohour
        val minute = nminute - ominute
        val second = nsecond - osecond


        if (hour >= 1 || hour == -23) {
            bit_hour!!.smoothFlip(1, 24, "HOUR", false)
        }

        if (minute >= 1 || minute == -59) {
            bit_minute!!.smoothFlip(1, 60, "MINUTE", false)
        }

        if (second >= 1 || second == -59) {
            bit_second!!.smoothFlip(1, 60, "SECOND", false)
        } //当下一秒变为0时减去上一秒是-59
    }
} 