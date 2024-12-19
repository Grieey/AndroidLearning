package com.learning.androidlearning.sample.dialog

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.learning.androidlearning.R

class DialogDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog_demo)

        findViewById<Button>(R.id.showRedPacketTipButton).setOnClickListener {
            RedPacketTipDialog.show(supportFragmentManager, "发布后100%得到新春红包哦~")
        }
    }
} 