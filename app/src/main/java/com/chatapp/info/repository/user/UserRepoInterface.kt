package com.chatapp.info.repository.user

import androidx.lifecycle.LiveData
import com.chatapp.info.data.User
import com.chatapp.info.utils.Result
import com.google.firebase.auth.FirebaseAuth



interface UserRepoInterface {

    suspend fun signUp(user: User)
    suspend fun login(user: User, rememberMe: Boolean)
    suspend fun signOut()
    suspend fun deleteUser(userId: String)
    fun observeLocalUser(userId: String): LiveData<Result<User>?>
    suspend fun getUser(userId: String):User?
    suspend fun hardRefreshUsersData(){}
    suspend fun observeAndRefreshUser(userId: String)
    suspend fun insertChat(chat: User.Chat)
    suspend fun deleteChat(chat: User.Chat)
    suspend fun updateChat(chat: List<User.Chat>)
    fun isRememberMeOn(): Boolean
    suspend fun getAllUsers(): List<User>?
    suspend fun insertMultiUsers(users: List<User>)




}