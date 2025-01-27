package com.learning.androidlearning.demo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.learning.androidlearning.R

class ConstraintLayoutDemoActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_constraint_demo)
    }
    
    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, ConstraintLayoutDemoActivity::class.java))
        }
    }
} 