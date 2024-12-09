package com.learning.androidlearning.sample.schedule.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE dayOfWeek = :day")
    fun getCoursesByDay(day: Int): Flow<List<Course>>

    @Query("""
        SELECT * FROM courses 
        WHERE createTime BETWEEN :startDate AND :endDate
        ORDER BY createTime ASC
    """)
    fun getCoursesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)
} 