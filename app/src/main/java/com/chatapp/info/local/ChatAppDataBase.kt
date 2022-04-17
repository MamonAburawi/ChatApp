package com.chatapp.info.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chatapp.info.data.Chat
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.local.api.ChatApi
import com.chatapp.info.local.api.MessageApi
import com.chatapp.info.local.api.UserApi
import com.chatapp.info.utils.DateTypeConverter
import com.chatapp.info.utils.ListTypeConverter
import com.chatapp.info.utils.ObjectListDataTypeConverter

@Database(entities = [User::class, Message::class, Chat::class], version = 3)
@TypeConverters( ListTypeConverter::class,DateTypeConverter::class,ObjectListDataTypeConverter::class)
abstract class ChatAppDataBase : RoomDatabase() {
    abstract fun userDao(): UserApi
    abstract fun messagesDao(): MessageApi
    abstract fun localChatApi(): ChatApi

    companion object {
        @Volatile
        private var INSTANCE: ChatAppDataBase? = null

        fun getInstance(context: Context): ChatAppDataBase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                ChatAppDataBase::class.java, "ChatDataBase19"
            )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()

    }
}