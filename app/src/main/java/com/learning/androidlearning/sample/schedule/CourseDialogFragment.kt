package com.learning.androidlearning.sample.schedule

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.learning.androidlearning.databinding.FragmentCourseDialogBinding
import com.learning.androidlearning.sample.schedule.data.Course
import com.skydoves.colorpickerview.listeners.ColorListener

class CourseDialogFragment : DialogFragment() {
    private var _binding: FragmentCourseDialogBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScheduleViewModel by activityViewModels { ScheduleViewModel.Factory }
    private var selectedColor = Color.BLUE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCourseDialogBinding.inflate(inflater, container, false)
        
        // Set dialog width
        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            // Set left and right margins
            setBackgroundDrawableResource(android.R.color.transparent)
        }
        
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Set width and margins in onStart
        dialog?.window?.apply {
            val width = (resources.displayMetrics.widthPixels * 0.9).toInt() // 90% screen width
            setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDurationSpinner()

        val row = arguments?.getInt(ARG_ROW) ?: 0
        val col = arguments?.getInt(ARG_COL) ?: 0
        val existingCourse = viewModel.getCourseAt(row, col)

        if (existingCourse != null) {
            setupEditMode(existingCourse)
        } else {
            setupCreateMode(row, col)
        }

        binding.colorPicker.setInitialColor(selectedColor)
        binding.colorPicker.colorListener = ColorListener { color, _ ->
            selectedColor = color
        }
    }

    private fun setupDurationSpinner() {
        val durations = (1..8).map { "$it Period(s)" }.toTypedArray()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            durations
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.durationSpinner.adapter = adapter
    }

    private fun setupEditMode(course: Course) {
        binding.apply {
            titleEdit.setText(course.title)
            descriptionEdit.setText(course.description)
            durationSpinner.setSelection(course.duration - 1)
            colorPicker.setInitialColor(course.color)
            selectedColor = course.color

            confirmButton.text = "Update"
            confirmButton.setOnClickListener {
                if (validateInput()) {
                    updateCourse(course)
                    dismiss()
                }
            }

            deleteButton.visibility = View.VISIBLE
            deleteButton.setOnClickListener {
                viewModel.deleteCourse(course)
                dismiss()
            }
        }
    }

    private fun setupCreateMode(row: Int, col: Int) {
        binding.apply {
            confirmButton.text = "Create"
            confirmButton.setOnClickListener {
                if (validateInput()) {
                    createCourse(row, col)
                    dismiss()
                }
            }
            deleteButton.visibility = View.GONE
        }
    }

    private fun validateInput(): Boolean {
        val title = binding.titleEdit.text.toString()
        return if (title.isBlank()) {
            binding.titleEdit.error = "Please enter course title"
            false
        } else {
            true
        }
    }

    private fun createCourse(row: Int, col: Int) {
        val course = Course(
            title = binding.titleEdit.text.toString().trim(),
            description = binding.descriptionEdit.text.toString().trim(),
            dayOfWeek = col,
            startTime = row + 1,
            // 0,
            duration = binding.durationSpinner.selectedItemPosition + 1,
            color = selectedColor
        )
        viewModel.addCourse(course)
    }

    private fun updateCourse(course: Course) {
        val updatedCourse = course.copy(
            title = binding.titleEdit.text.toString().trim(),
            description = binding.descriptionEdit.text.toString().trim(),
            duration = binding.durationSpinner.selectedItemPosition + 1,
            color = selectedColor
        )
        viewModel.updateCourse(updatedCourse)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ROW = "row"
        private const val ARG_COL = "col"

        fun newInstance(row: Int, col: Int) = CourseDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_ROW, row)
                putInt(ARG_COL, col)
            }
        }
    }
} 