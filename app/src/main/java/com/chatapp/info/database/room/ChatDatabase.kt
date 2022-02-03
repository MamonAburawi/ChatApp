//package com.chatapp.info.database.room
//
//import android.content.Context
//import androidx.room.*
//import androidx.room.migration.Migration
//import androidx.sqlite.db.SupportSQLiteDatabase
//import com.chatapp.info.data.DateConverter
//import com.chatapp.info.data.Message
//import com.chatapp.info.data.User
//
//
//@Database(entities = [User::class, Message::class], version = 1)
//@TypeConverters(DateConverter::class)
//abstract class ChatDatabase : RoomDatabase() {
//    companion object {
//
//        @Volatile
//        private var chatDatabase: ChatDatabase? = null
//
//        private val MIGRATION_1_2 by lazy {
//            object : Migration(1, 2) {
//                override fun migrate(database: SupportSQLiteDatabase) {
//                    database.execSQL("ALTER TABLE messages ADD COLUMN status INTEGER")
//                }
//            }
//        }
//
//        fun getDatabase(applicationContext: Context): ChatDatabase {
//            if (chatDatabase == null){
//                synchronized(ChatDatabase::class.java){
//                    val database = Room.databaseBuilder(applicationContext, ChatDatabase::class.java, "chatDatabase")
//                        .fallbackToDestructiveMigration() // you will need this code when you need to upgrade you version of database ..
////                    .addMigrations(MIGRATION_1_2)
//                        .build()
//                    chatDatabase = database
//                }
//            }
//
//            return chatDatabase!!
//        }
//    }
//    abstract fun userDao(): UserDao
//    abstract fun messageDao(): MessageDao
//}