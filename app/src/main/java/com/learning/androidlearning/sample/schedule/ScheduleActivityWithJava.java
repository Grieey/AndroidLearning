package com.learning.androidlearning.sample.schedule;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.learning.androidlearning.databinding.ActivityScheduleBinding;
import com.learning.androidlearning.sample.schedule.data.CourseJava;

public class ScheduleActivityWithJava extends AppCompatActivity {
    private ActivityScheduleBinding binding;
    private ScheduleViewModelJava viewModel;
    private ScheduleAdapterWithJava adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScheduleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViewModel();
        setupRecyclerView();
        observeCourses();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ScheduleViewModelJava.class);
    }

    private void setupRecyclerView() {
        adapter = new ScheduleAdapterWithJava(this::showCourseDialog);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 6);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });

        binding.scheduleRecyclerView.setLayoutManager(layoutManager);
        binding.scheduleRecyclerView.setAdapter(adapter);
        binding.scheduleRecyclerView.setHasFixedSize(true);
    }

    private void observeCourses() {
        viewModel.getCourses().observe(this, courses -> {
            if (courses != null) {
                adapter.updateCourses(courses);
            }
        });
    }

    private void showCourseDialog(int row, int col) {
        CourseDialogFragmentWithJava.newInstance(row, col)
                .show(getSupportFragmentManager(), "course_dialog");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
} 