package com.chatapp.info.utils

import androidx.room.TypeConverter
import java.util.*

class DateTypeConverter {
    @TypeConverter
    fun from(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    @TypeConverter
    fun to(date: Date?): Long? {
        return date?.time
    }
}
