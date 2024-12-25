package com.learning.androidlearning.sample.flipclock

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.learning.androidlearning.R

class FlipClockDemoActivity : AppCompatActivity() {
    private var isLandscape = true
    private var isAnimating = false

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
    }

    // 保存状态
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isLandscape", isLandscape)
    }
} 