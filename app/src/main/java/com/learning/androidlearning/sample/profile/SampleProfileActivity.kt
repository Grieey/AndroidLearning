package com.learning.androidlearning.sample.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.learning.androidlearning.R

class SampleProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample_profile)

        // Setup RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.photoGrid)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        // Sample photo list (replace with your actual photo resources)
        val photos = listOf(
            R.drawable.sample_photo_1,
            R.drawable.sample_photo_2,
            R.drawable.sample_photo_3,
        )

        val adapter = PhotoAdapter(photos)
        recyclerView.adapter = adapter

        // Setup BottomNavigationView
        val bottomNav: BottomNavigationView = findViewById(R.id.bottomNavigation)
        bottomNav.itemIconTintList = null
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Handle home navigation
                    true
                }

                R.id.nav_search -> {
                    // Handle search navigation
                    true
                }

                R.id.nav_add -> {
                    // Handle add navigation
                    true
                }

                R.id.nav_chat -> {
                    // Handle notifications navigation
                    true
                }

                R.id.nav_profile -> {
                    // Handle profile navigation
                    true
                }

                else -> false
            }
        }
    }
}