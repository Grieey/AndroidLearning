package com.learning.androidlearning.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.learning.androidlearning.R
import com.learning.androidlearning.demo.ConstraintLayoutDemoActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment.newInstance())
                .commit()
        }

        findViewById<Button>(R.id.btnConstraintDemo).setOnClickListener {
            ConstraintLayoutDemoActivity.start(this)
        }
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, HomeActivity::class.java))
        }
    }
} 