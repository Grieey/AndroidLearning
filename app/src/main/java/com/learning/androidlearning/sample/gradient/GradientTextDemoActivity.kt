package com.learning.androidlearning.sample.gradient

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.learning.androidlearning.R

class GradientTextDemoActivity : AppCompatActivity() {

    private lateinit var gradientTextView: GradientTextView
    private lateinit var editText: EditText
    private lateinit var sizeSeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gradient_text_demo)

        gradientTextView = findViewById(R.id.gradientTextView)
        editText = findViewById(R.id.editText)
        sizeSeekBar = findViewById(R.id.sizeSeekBar)

        // Setup text change listener
        editText.addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                    ) {}
                    override fun onTextChanged(
                            s: CharSequence?,
                            start: Int,
                            before: Int,
                            count: Int
                    ) {}
                    override fun afterTextChanged(s: Editable?) {
                        s?.toString()?.let { gradientTextView.setText(it) }
                    }
                }
        )

        // Setup size change listener
        sizeSeekBar.setOnSeekBarChangeListener(
                object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                            seekBar: SeekBar?,
                            progress: Int,
                            fromUser: Boolean
                    ) {
                        gradientTextView.setTextSize((progress + 20).toFloat())
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                }
        )
    }
}
