package com.chatapp.info.local.user

import androidx.room.*
import com.chatapp.info.data.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultiUsers(users: List<User>)

    @Query("SELECT * FROM user WHERE userId = :userId")
    suspend fun getUserById(userId: String): User?

    @Update(entity = User::class)
    suspend fun updateUser(user: User)

    @Query("delete from user where userId = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("DELETE FROM user")
    suspend fun deleteUser()

    @Query("SELECT * FROM user")
    suspend fun getAllUsers(): List<User>
}