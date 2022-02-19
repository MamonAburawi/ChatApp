package com.chatapp.info.data

import androidx.room.TypeConverter
import java.util.*

object DateConverter {
    @TypeConverter
    fun from(value: Long?): Date? {
        return value?.let { Date(it) }
    }
    @TypeConverter
    fun to(date: Date?): Long? {
        return date?.time
    }
}
