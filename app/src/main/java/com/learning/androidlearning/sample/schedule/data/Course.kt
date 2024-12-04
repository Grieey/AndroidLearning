package com.learning.androidlearning.sample.schedule.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val dayOfWeek: Int, // 1-5 mon to fri
    val startTime: Int, // 1-8 period 1 to 8
    val duration: Int,
    val color: Int
) 