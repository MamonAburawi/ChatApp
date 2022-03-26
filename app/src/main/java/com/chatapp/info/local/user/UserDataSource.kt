package com.chatapp.info.local.user

import androidx.lifecycle.LiveData
import com.chatapp.info.data.User
import com.chatapp.info.utils.Result

interface UserDataSource {

    suspend fun addUser(user: User)

    suspend fun updateUser(user: User)

    suspend fun deleteAllUsers()

    suspend fun deleteUserById(userId: String)

    suspend fun getUserById (userId: String): User?

    suspend fun hardRefreshUsers(): Result<List<User>>

    suspend fun getUsersData(): List<User>?

    suspend fun insertChat(chat: User.Chat)

    suspend fun deleteChat(chat: User.Chat,forBoth: Boolean = false){}

    suspend fun updateChat(chat: List<User.Chat>){}

    suspend fun insertMultiUsers(users: List<User>){}

    fun observeLocalUser(userId: String):  LiveData<Result<User>?>

    suspend fun observeRemoteUser(userId: String,update:(User)-> Unit){}


}