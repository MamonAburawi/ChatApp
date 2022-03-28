package com.chatapp.info.utils

import androidx.room.TypeConverter
import com.chatapp.info.data.Chat
import com.chatapp.info.data.User
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

class ObjectListDataTypeConverter {

    @TypeConverter
    fun stringToChatObjectList(data: String?): List<Chat> {
        if (data.isNullOrBlank()) {
            return emptyList()
        }
        val listType = object : TypeToken<List<Chat>>() {}.type
        val gson = Gson()
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun chatObjectListToString(chat: List<Chat>): String {
        if (chat.isEmpty()) {
            return ""
        }
        val gson = Gson()
        val listType = object : TypeToken<List<Chat>>() {}.type
        return gson.toJson(chat, listType)
    }



}