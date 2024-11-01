package com.learning.androidlearning.sample.schedule

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.learning.androidlearning.R

class ScheduleGridActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduleGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_grid)

        recyclerView = findViewById(R.id.scheduleRecyclerView)
        setupScheduleGrid()
    }

    private fun setupScheduleGrid() {
        // 6 columns (time column + Monday to Friday)
        val columnCount = 6
        
        // Initialize data
        val scheduleItems = createInitialScheduleData()
        
        adapter = ScheduleGridAdapter(scheduleItems)
        recyclerView.layoutManager = GridLayoutManager(this, columnCount)
        recyclerView.adapter = adapter
    }

    private fun createInitialScheduleData(): List<ScheduleItem> {
        val items = mutableListOf<ScheduleItem>()
        
        // Add headers (first row)
        items.add(ScheduleItem("Time", isHeader = true))
        items.add(ScheduleItem("Mon", isHeader = true))
        items.add(ScheduleItem("Tue", isHeader = true))
        items.add(ScheduleItem("Wed", isHeader = true))
        items.add(ScheduleItem("Thu", isHeader = true))
        items.add(ScheduleItem("Fri", isHeader = true))

        // Add time slots and corresponding empty course cells
        val timeSlots = listOf(
            "8:00-9:00", "9:00-10:00", "10:00-11:00", "11:00-12:00",
            "13:00-14:00", "14:00-15:00", "15:00-16:00", "16:00-17:00"
        )

        timeSlots.forEach { time ->
            // Add time column
            items.add(ScheduleItem(time, isTimeSlot = true))
            // Add 5 empty course cells for each time slot
            repeat(5) {
                items.add(ScheduleItem("", isEditable = true))
            }
        }

        return items
    }
} 