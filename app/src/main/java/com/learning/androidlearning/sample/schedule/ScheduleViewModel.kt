package com.learning.androidlearning.sample.schedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.learning.androidlearning.sample.schedule.data.Course
import com.learning.androidlearning.sample.schedule.data.CourseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CourseDatabase.getDatabase(application)
    private val courseDao = database.courseDao()

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses

    init {
//        loadCourses()
        loadCurrentWeekCourses()
    }

    private fun loadCourses() {
        viewModelScope.launch {
            courseDao.getAllCourses().collect {
                _courses.value = it
            }
        }
    }

    fun addCourse(course: Course) {
        viewModelScope.launch {
            courseDao.insertCourse(course)
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            courseDao.updateCourse(course)
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            courseDao.deleteCourse(course)
        }
    }

    fun getCourseAt(row: Int, col: Int): Course? {
        return courses.value.find { course ->
            val startRow = course.startTime - 1
            val endRow = startRow + course.duration - 1
            course.dayOfWeek == col && row in startRow..endRow
        }
    }

    /**
     * Load courses within the specified date range
     * @param startDate start date of the range
     * @param endDate end date of the range
     */
    fun loadCoursesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime) {
        viewModelScope.launch {
            courseDao.getCoursesByDateRange(startDate, endDate).collect {
                _courses.value = it
            }
        }
    }

    /**
     * Load courses for current week
     */
    fun loadCurrentWeekCourses() {
        val now = LocalDateTime.now()
        val startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .withHour(0).withMinute(0).withSecond(0)
        val endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY))
            .withHour(23).withMinute(59).withSecond(59)
        
        loadCoursesByDateRange(startOfWeek, endOfWeek)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as Application)
                ScheduleViewModel(application)
            }
        }
    }
} 