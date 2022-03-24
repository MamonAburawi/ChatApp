package com.chatapp.info.local.user

import android.util.Log
import com.chatapp.info.data.User
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

    override suspend fun getUserById(userId: String): User? {
      return try {
          withContext(ioDispatcher){
              userDao.getUserById(userId)
          }
      }catch (ex: Exception){
          return null
      }

    }

    override suspend fun getAllUsers(): List<User> {
        return try {
            Log.d(TAG,"users is getting")
            withContext(ioDispatcher){
                userDao.getAllUsers()
            }
        }catch (ex: Exception){
            return emptyList()
        }
    }

    override suspend fun insertChat(chat: User.Chat) {
        try {
            val senderData = getUserById(chat.senderId)
            val recipientData = getUserById(chat.recipientId)
            if (senderData != null && recipientData != null) {
                val senderChats = senderData.chats.toMutableList()
                val recipientChats = recipientData.chats.toMutableList()
                senderChats.add(chat)
                recipientChats.add(chat)

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

    suspend fun deleteChat(chat: User.Chat) {
        try {
            val uData = getUserById(chat.senderId)
            if (uData != null) {
                val chats = uData.chats.toMutableList()
                chats.remove(chat)
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