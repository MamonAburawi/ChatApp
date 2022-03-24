package com.chatapp.info.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.local.message.MessageDao
import com.chatapp.info.local.user.UserDao
import com.chatapp.info.utils.DateTypeConverter
import com.chatapp.info.utils.ListTypeConverter
import com.chatapp.info.utils.ObjectListDataTypeConverter

@Database(entities = [User::class, Message::class], version = 2)
@TypeConverters( ListTypeConverter::class,DateTypeConverter::class,ObjectListDataTypeConverter::class)
abstract class ChatAppDataBase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messagesDao(): MessageDao

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
                ChatAppDataBase::class.java, "ShoppingAppDb"
            )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()

    }
}