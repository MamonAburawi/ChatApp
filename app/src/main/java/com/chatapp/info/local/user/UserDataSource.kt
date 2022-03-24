package com.chatapp.info.local.user

import com.chatapp.info.data.User
import com.chatapp.info.utils.Result

interface UserDataSource {

    suspend fun addUser(user: User)

    suspend fun updateUser(user: User)

    suspend fun deleteAllUsers()

    suspend fun deleteUserById(userId: String)

    suspend fun getUserById (userId: String): User?

    suspend fun getAllUsers(): List<User>

    suspend fun insertChat(chat: User.Chat)

    suspend fun deleteChat(chat: User.Chat,forBoth: Boolean = false){}

    suspend fun insertMultiUsers(users: List<User>){}




}