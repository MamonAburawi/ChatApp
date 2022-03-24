package com.chatapp.info.repository.user

import com.chatapp.info.data.User
import com.google.firebase.auth.FirebaseAuth



interface UserRepoInterface {

    suspend fun signUp(user: User)
    suspend fun login(user: User, rememberMe: Boolean)
    suspend fun signOut()
    suspend fun deleteUser(userId: String)
    suspend fun getUser(userId: String):User?
    suspend fun hardRefreshData()
    suspend fun insertChat(chat: User.Chat)
    suspend fun deleteChat(chat: User.Chat)
    fun isRememberMeOn(): Boolean
    suspend fun getAllUsers(): List<User>
    suspend fun insertMultiUsers(users: List<User>)




}