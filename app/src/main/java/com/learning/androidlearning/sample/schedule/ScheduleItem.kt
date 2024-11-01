package com.learning.androidlearning.sample.schedule

data class ScheduleItem(
    var content: String,
    val isHeader: Boolean = false,
    val isTimeSlot: Boolean = false,
    val isEditable: Boolean = false
) 