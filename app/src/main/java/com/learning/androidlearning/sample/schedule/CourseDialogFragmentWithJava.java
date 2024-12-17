package com.learning.androidlearning.sample.schedule;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.learning.androidlearning.databinding.FragmentCourseDialogBinding;
import com.learning.androidlearning.sample.schedule.data.CourseJava;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorListener;

public class CourseDialogFragmentWithJava extends DialogFragment {
    private static final String ARG_ROW = "row";
    private static final String ARG_COL = "col";

    private FragmentCourseDialogBinding binding;
    private ScheduleViewModelJava viewModel;
    private int selectedColor = Color.BLUE;

    public static CourseDialogFragmentWithJava newInstance(int row, int col) {
        CourseDialogFragmentWithJava fragment = new CourseDialogFragmentWithJava();
        Bundle args = new Bundle();
        args.putInt(ARG_ROW, row);
        args.putInt(ARG_COL, col);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCourseDialogBinding.inflate(inflater, container, false);
        
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(requireActivity()).get(ScheduleViewModelJava.class);
        setupDurationSpinner();
        setupColorPicker();

        int row = getArguments() != null ? getArguments().getInt(ARG_ROW) : 0;
        int col = getArguments() != null ? getArguments().getInt(ARG_COL) : 0;
        CourseJava existingCourse = viewModel.getCourseAt(row, col);

        if (existingCourse != null) {
            setupEditMode(existingCourse);
        } else {
            setupCreateMode(row, col);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.9);
            getDialog().getWindow().setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private void setupDurationSpinner() {
        String[] durations = new String[8];
        for (int i = 0; i < 8; i++) {
            durations[i] = (i + 1) + " Period(s)";
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                durations
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.durationSpinner.setAdapter(adapter);
    }

    private void setupColorPicker() {
        binding.colorPicker.setInitialColor(selectedColor);
        binding.colorPicker.setColorListener((ColorListener) (color, fromUser) -> selectedColor = color);
    }

    private void setupEditMode(CourseJava course) {
        binding.titleEdit.setText(course.getTitle());
        binding.descriptionEdit.setText(course.getDescription());
        binding.durationSpinner.setSelection(course.getDuration() - 1);
        binding.colorPicker.setInitialColor(course.getColor());
        selectedColor = course.getColor();

        binding.confirmButton.setText("Update");
        binding.confirmButton.setOnClickListener(v -> {
            if (validateInput()) {
                updateCourse(course);
                dismiss();
            }
        });

        binding.deleteButton.setVisibility(View.VISIBLE);
        binding.deleteButton.setOnClickListener(v -> {
            viewModel.deleteCourse(course);
            dismiss();
        });
    }

    private void setupCreateMode(int row, int col) {
        binding.confirmButton.setText("Create");
        binding.confirmButton.setOnClickListener(v -> {
            if (validateInput()) {
                createCourse(row, col);
                dismiss();
            }
        });
        binding.deleteButton.setVisibility(View.GONE);
    }

    private boolean validateInput() {
        String title = binding.titleEdit.getText().toString();
        if (title.trim().isEmpty()) {
            binding.titleEdit.setError("Please enter course title");
            return false;
        }
        return true;
    }

    private void createCourse(int row, int col) {
        CourseJava course = new CourseJava(
                0,
                binding.titleEdit.getText().toString().trim(),
                binding.descriptionEdit.getText().toString().trim(),
                col,
                row + 1,
                binding.durationSpinner.getSelectedItemPosition() + 1,
                selectedColor
        );
        viewModel.addCourse(course);
    }

    private void updateCourse(CourseJava course) {
        CourseJava updatedCourse = new CourseJava(
                course.getId(),
                binding.titleEdit.getText().toString().trim(),
                binding.descriptionEdit.getText().toString().trim(),
                course.getDayOfWeek(),
                course.getStartTime(),
                binding.durationSpinner.getSelectedItemPosition() + 1,
                selectedColor
        );
        viewModel.updateCourse(updatedCourse);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 