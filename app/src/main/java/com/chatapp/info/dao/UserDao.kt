//package com.chatapp.info.dao
//
//
//import androidx.room.*
//import com.chatapp.info.data.User
//
//
//@Dao
//interface UserDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertUser(user: User)
//
//    @Update
//    suspend fun updateUser(user: User)
//
//    @Delete
//    suspend fun deleteUser(user: User)
//
//    @Query("SELECT * FROM users")
//    fun loadAllUsers(): List<User>
//
//}