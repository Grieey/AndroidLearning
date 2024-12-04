package com.learning.androidlearning.sample.schedule

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.learning.androidlearning.databinding.ActivityScheduleBinding
import kotlinx.coroutines.launch

class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding
    private val viewModel: ScheduleViewModel by viewModels { ScheduleViewModel.Factory }
    private lateinit var adapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeCourses()
    }

    private fun setupRecyclerView() {
        adapter = ScheduleAdapter { row, col ->
            showCourseDialog(row, col)
        }

        binding.scheduleRecyclerView.apply {
            layoutManager = GridLayoutManager(this@ScheduleActivity, 6).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return 1
                    }
                }
            }
            adapter = this@ScheduleActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeCourses() {
        lifecycleScope.launch {
            viewModel.courses.collect { courses ->
                adapter.updateCourses(courses)
            }
        }
    }

    private fun showCourseDialog(row: Int, col: Int) {
        CourseDialogFragment.newInstance(row, col)
            .show(supportFragmentManager, "course_dialog")
    }
} 