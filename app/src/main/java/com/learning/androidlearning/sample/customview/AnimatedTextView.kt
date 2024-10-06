package com.learning.androidlearning.sample.customview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.learning.androidlearning.R
import kotlinx.coroutines.*

class AnimatedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val textView1: TextView
    private val textView2: TextView
    private var currentTextView: TextView
    private var nextTextView: TextView
    private val fadeOutAnimation: Animation
    private val fadeInAnimation: Animation
    private var animationJob: Job? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_animated_text, this, true)
        textView1 = findViewById(R.id.textView1)
        textView2 = findViewById(R.id.textView2)
        currentTextView = textView1
        nextTextView = textView2

        fadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out_up)
        fadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in_up)
    }

    fun startAnimation(texts: List<String>) {
        animationJob?.cancel()
        animationJob = CoroutineScope(Dispatchers.Main).launch {
            var index = 0
            while (isActive) {
                val text = texts[index]
                animateTextChange(text)
                delay(2000)
                index = (index + 1) % texts.size
            }
        }
    }

    private fun animateTextChange(newText: String) {
        nextTextView.text = newText
        nextTextView.isVisible = true
        currentTextView.startAnimation(fadeOutAnimation)
        nextTextView.startAnimation(fadeInAnimation)

        val temp = currentTextView
        currentTextView = nextTextView
        nextTextView = temp
        nextTextView.isVisible = false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animationJob?.cancel()
    }
}