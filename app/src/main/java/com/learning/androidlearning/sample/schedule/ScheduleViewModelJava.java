package com.learning.androidlearning.sample.schedule;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.learning.androidlearning.sample.schedule.data.CourseJava;
import com.learning.androidlearning.sample.schedule.data.CourseDatabaseJava;
import com.learning.androidlearning.sample.schedule.data.CourseDaoJava;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScheduleViewModelJava extends AndroidViewModel {
    private final CourseDatabaseJava database;
    private final CourseDaoJava courseDao;
    private final ExecutorService executorService;
    private final LiveData<List<CourseJava>> courses;

    public ScheduleViewModelJava(@NonNull Application application) {
        super(application);
        database = CourseDatabaseJava.getDatabase(application);
        courseDao = database.courseDao();
        executorService = Executors.newSingleThreadExecutor();
        courses = courseDao.getAllCourses();
        loadCurrentWeekCourses();
    }

    public LiveData<List<CourseJava>> getCourses() {
        return courses;
    }

    public void addCourse(CourseJava course) {
        executorService.execute(() -> courseDao.insertCourse(course));
    }

    public void updateCourse(CourseJava course) {
        executorService.execute(() -> courseDao.updateCourse(course));
    }

    public void deleteCourse(CourseJava course) {
        executorService.execute(() -> courseDao.deleteCourse(course));
    }

    public CourseJava getCourseAt(int row, int col) {
        List<CourseJava> currentCourses = courses.getValue();
        if (currentCourses != null) {
            for (CourseJava course : currentCourses) {
                int startRow = course.getStartTime() - 1;
                int endRow = startRow + course.getDuration() - 1;
                if (course.getDayOfWeek() == col && row >= startRow && row <= endRow) {
                    return course;
                }
            }
        }
        return null;
    }

    public void loadCoursesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        courseDao.getCoursesByDateRange(startDate, endDate);
    }

    public void loadCurrentWeekCourses() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
                .withHour(23).withMinute(59).withSecond(59);
        
        loadCoursesByDateRange(startOfWeek, endOfWeek);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
} 