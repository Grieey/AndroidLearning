package com.learning.androidlearning.sample.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.learning.androidlearning.R

class SampleCustomViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_custom_view)

        val animatedTextView = findViewById<AnimatedTextView>(R.id.animatedTextView)
        val texts = listOf("Hello", "World", "This", "Is", "Animated", "Text")
        animatedTextView.startAnimation(texts)
    }
}