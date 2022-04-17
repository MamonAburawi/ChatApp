package com.chatapp.info.repository.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.chatapp.info.data.Chat
import com.chatapp.info.data.User
import com.chatapp.info.local.api.UserApi
import com.chatapp.info.utils.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalUserRepository (private val userApi: UserApi) {

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    companion object{
        const val TAG = "UserLocalUser"
    }

    suspend fun addUser(user: User) {
        withContext(ioDispatcher){
            userApi.addUser(user)
        }
    }

    suspend fun updateUser(user: User) {
        withContext(ioDispatcher){
            userApi.updateUser(user)
        }
    }

    suspend fun deleteAllUsers() {
        withContext(ioDispatcher){
            userApi.deleteUser()
        }
    }

    suspend fun getUserChats(userId: String): Result<List<Chat>> {
        return Result.Success(emptyList())
    }

    suspend fun deleteUserById(userId: String) {
        withContext(ioDispatcher){
            userApi.deleteUserById(userId)
        }
    }

    suspend fun insertMultiUsers(users: List<User>) {
        withContext(ioDispatcher){
            userApi.insertMultiUsers(users)
        }
    }

    fun observeLocalUser(userId: String): LiveData<Result<User>?> {
        return try {
            Transformations.map(userApi.observeUser(userId)) {
                Result.Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Result.Error(e)
            }
        }
    }

    suspend fun getUserById(userId: String): User? {
      return try {
          withContext(ioDispatcher){
              userApi.getUserById(userId)
          }
      }catch (ex: Exception){
          return null
      }

    }

    suspend fun getUsersData(): List<User>? {
        Log.d(TAG,"users is getting")
        return try {
            userApi.getAllUsers()
        }catch (ex: Exception){
            emptyList()
        }
    }

    suspend fun hardRefreshUsers(): Result<List<User>>{
     return Result.Success(emptyList())
    }

    fun observeUsers(): LiveData<Result<List<User>?>> {
        return try {
            Transformations.map(userApi.observeUsers()) {
                Result.Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Result.Error(e)
            }
        }
    }

    suspend fun insertChat(chat: Chat) {
        try {
            val senderData = getUserById(chat.senderId)
            val recipientData = getUserById(chat.recipientId)
            if (senderData != null && recipientData != null) {
                val senderChats = senderData.chats.toMutableList()
                val recipientChats = recipientData.chats.toMutableList()
                senderChats.add(chat.chatId)
                recipientChats.add(chat.chatId)

                recipientData.chats = recipientChats
                senderData.chats = senderChats
                userApi.updateUser(senderData)
                userApi.updateUser(recipientData)
            }else{
                throw Exception("User Not Found!")
            }
        }catch (e: Exception) {
            Log.d(TAG, "onSetChat: Error Occurred, ${e.message}")
            throw e
        }
    }

    suspend fun deleteChat(chat: Chat) {
        try {
            val uData = getUserById(chat.senderId)
            if (uData != null) {
                val chats = uData.chats.toMutableList()
                chats.remove(chat.chatId)
                uData.chats = chats
                userApi.updateUser(uData)
            }else{
                throw Exception("User Not Found!")
            }
        }catch (e: Exception) {
            Log.d(TAG, "onSetChat: Error Occurred, ${e.message}")
            throw e
        }
    }


}