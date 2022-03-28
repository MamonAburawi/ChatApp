package com.chatapp.info.local.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.chatapp.info.data.Chat
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.utils.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserLocalDataSource internal constructor(
    private val userDao: UserDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
): UserDataSource{

    companion object{
        const val TAG = "UserLocalUser"
    }


    override suspend fun addUser(user: User) {
        withContext(ioDispatcher){
            userDao.addUser(user)
        }
    }

    override suspend fun updateUser(user: User) {
        withContext(ioDispatcher){
            userDao.updateUser(user)
        }
    }


    override suspend fun deleteAllUsers() {
        withContext(ioDispatcher){
            userDao.deleteUser()
        }
    }

    override suspend fun getUserChats(userId: String): Result<List<Chat>> {
        return Result.Success(emptyList())
    }

    override suspend fun deleteUserById(userId: String) {
        withContext(ioDispatcher){
            userDao.deleteUserById(userId)
        }
    }

    override suspend fun insertMultiUsers(users: List<User>) {
        withContext(ioDispatcher){
            userDao.insertMultiUsers(users)
        }
    }
    override fun observeLocalUser(userId: String): LiveData<Result<User>?> {
        return try {
            Transformations.map(userDao.observeUser(userId)) {
                Result.Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Result.Error(e)
            }
        }
    }


    override suspend fun getUserById(userId: String): User? {
      return try {
          withContext(ioDispatcher){
              userDao.getUserById(userId)
          }
      }catch (ex: Exception){
          return null
      }

    }

    override suspend fun getUsersData(): List<User>? {
        Log.d(TAG,"users is getting")
        return try {
            userDao.getAllUsers()
        }catch (ex: Exception){
            emptyList()
        }
    }

    override suspend fun hardRefreshUsers(): Result<List<User>>{
     return Result.Success(emptyList())
    }

    override  fun observeUsers(): LiveData<Result<List<User>?>> {
        return try {
            Transformations.map(userDao.observeUsers()) {
                Result.Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Result.Error(e)
            }
        }
    }

    override suspend fun insertChat(chat: Chat) {
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
                userDao.updateUser(senderData)
                userDao.updateUser(recipientData)
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
                userDao.updateUser(uData)
            }else{
                throw Exception("User Not Found!")
            }
        }catch (e: Exception) {
            Log.d(TAG, "onSetChat: Error Occurred, ${e.message}")
            throw e
        }
    }




}