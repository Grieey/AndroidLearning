package com.learning.androidlearning.sample.schedule.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.time.LocalDateTime;
import java.util.List;

@Dao
public interface CourseDaoJava {
    @Query("SELECT * FROM courses_java")
    LiveData<List<CourseJava>> getAllCourses();

    @Query("SELECT * FROM courses_java WHERE dayOfWeek = :day")
    LiveData<List<CourseJava>> getCoursesByDay(int day);

    @Query("SELECT * FROM courses_java WHERE createTime BETWEEN :startDate AND :endDate ORDER BY createTime ASC")
    LiveData<List<CourseJava>> getCoursesByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCourse(CourseJava course);

    @Update
    void updateCourse(CourseJava course);

    @Delete
    void deleteCourse(CourseJava course);
} 