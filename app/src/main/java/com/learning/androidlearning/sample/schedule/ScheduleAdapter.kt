package com.learning.androidlearning.sample.schedule

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.learning.androidlearning.databinding.ItemScheduleCellBinding
import com.learning.androidlearning.sample.schedule.data.Course

class ScheduleAdapter(
    private val onCellClick: (Int, Int) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.CellViewHolder>() {

    private var courses = listOf<Course>()
    private val timeSlots = 9 // 8 time slots + 1 header row
    private val daysOfWeek = 6 // 5 days + 1 time column
    private val cells = Array(timeSlots) { Array(daysOfWeek) { null as Course? } }

    private val timeSlotLabels = arrayOf(
        "",  // First cell empty
        "1st Period 8:00-8:45",
        "2nd Period 8:55-9:40",
        "3rd Period 10:00-10:45",
        "4th Period 10:55-11:40",
        "5th Period 14:00-14:45",
        "6th Period 14:55-15:40",
        "7th Period 16:00-16:45",
        "8th Period 16:55-17:40"
    )

    private val weekDayLabels = arrayOf("Period\\Date", "MON", "TUE", "WED", "THU", "FRI")

    class CellViewHolder(
        private val binding: ItemScheduleCellBinding,
        private val onCellClick: (Int, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course?, row: Int, col: Int, isHeader: Boolean, headerText: String) {
            when {
                isHeader -> {
                    binding.cellText.text = headerText
                    binding.root.setCardBackgroundColor(Color.LTGRAY)
                    binding.root.setOnClickListener(null)
                    binding.cellText.setTextColor(Color.BLACK)
                }

                course != null -> {
                    binding.cellText.text = course.title
                    binding.root.setCardBackgroundColor(course.color)
                    binding.root.setOnClickListener { onCellClick(row - 1, col) }
                    binding.cellText.setTextColor(Color.WHITE)
                }

                else -> {
                    binding.cellText.text = ""
                    binding.root.setCardBackgroundColor(Color.WHITE)
                    // Only non-header cells can be clicked
                    if (row > 0 && col > 0) {
                        binding.root.setOnClickListener { onCellClick(row - 1, col) }
                    } else {
                        binding.root.setOnClickListener(null)
                    }
                    binding.cellText.setTextColor(Color.BLACK)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val binding = ItemScheduleCellBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CellViewHolder(binding) { row, col -> onCellClick(row, col) }
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val row = position / daysOfWeek
        val col = position % daysOfWeek

        when {
            // First row shows weekdays
            row == 0 -> {
                holder.bind(null, row, col, true, weekDayLabels[col])
            }
            // First column shows time slots
            col == 0 -> {
                holder.bind(null, row, col, true, timeSlotLabels[row])
            }
            // Other cells show courses
            else -> {
                val course = cells[row - 1][col]
                holder.bind(course, row, col, false, "")
            }
        }
    }

    override fun getItemCount() = timeSlots * daysOfWeek

    fun updateCourses(newCourses: List<Course>) {
        courses = newCourses
        // Reset cells
        cells.forEach { it.fill(null) }

        // Fill courses
        for (course in courses) {
            val startRow = course.startTime - 1
            for (i in 0 until course.duration) {
                if (startRow + i < timeSlots - 1) {
                    cells[startRow + i][course.dayOfWeek] = course
                }
            }
        }

        notifyDataSetChanged()
    }
} 