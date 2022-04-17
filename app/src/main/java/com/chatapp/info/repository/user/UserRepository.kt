package com.chatapp.info.repository.user

import android.util.Log
import androidx.lifecycle.LiveData
import com.chatapp.info.data.Chat
import com.chatapp.info.data.User
import com.chatapp.info.repository.user.RemoteUserRepository
import com.chatapp.info.utils.ChatAppSessionManager
import com.chatapp.info.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import kotlinx.coroutines.*


class UserRepository(
    private val localUserRepository: LocalUserRepository,
    private val remoteUserRepository: RemoteUserRepository,
    private val sessionManager: ChatAppSessionManager
) {

    private val userId = sessionManager.getUserIdFromSession()

    companion object {
        private const val TAG = "UserRepository"
    }

    private var firebaseAuth = FirebaseAuth.getInstance()

    suspend fun insertMultiUsers(users: List<User>) {
        localUserRepository.insertMultiUsers(users)
    }

    suspend fun signUp(user: User) {
        sessionManager.createLoginSession(user.userId, user.name, false)
        Log.d(TAG, "on SignUp: Updating user in Local Source")
        localUserRepository.addUser(user)
        Log.d(TAG, "on SignUp: Updating userdata on Remote Source")
        remoteUserRepository.addUser(user)
    }

    suspend fun observeUserChatsIds(userId: String, ChatsIds: (List<Chat>) -> Unit) {

        remoteUserRepository.observeUserChatsIds(userId, ChatsIds)
//        val user = userLocalDataSource.getUserById(userId)
//        val chats = user?.ChatsIds?.toMutableSet()
//        user?.chats = chats!!.toList()
//
//        userRemoteDataSource.updateUser(user)
    }

    suspend fun login(user: User, rememberMe: Boolean) {
        sessionManager.createLoginSession(
            user.userId,
            user.name,
            rememberMe
        )
        localUserRepository.addUser(user)
    }

    suspend fun signOut() {
		sessionManager.logoutFromSession()
		localUserRepository.deleteAllUsers()

        firebaseAuth.signOut()
    }

    suspend fun getUserChats(userId: String): Result<List<Chat>> {
      return remoteUserRepository.getUserChats(userId)
    }

    suspend fun updateUser(user: User) {
        return supervisorScope {
            val remoteRes = async {
              remoteUserRepository.updateUser(user)
            }
            val localRes = async {
              localUserRepository.updateUser(user)
            }
            try {
                remoteRes.await()
                localRes.await()
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    suspend fun deleteUser(userId: String) {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onDelete: deleting User from remote source")
                remoteUserRepository.deleteUserById(userId)
            }
            val localRes = async {
                Log.d(TAG, "onDelete: deleting User from local source")
                localUserRepository.deleteUserById(userId)
            }
            try {
                remoteRes.await()
                localRes.await()
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    fun observeLocalUser(userId: String): LiveData<Result<User>?> {
        return localUserRepository.observeLocalUser(userId)
    }

    fun observeUsers(): LiveData<Result<List<User>?>> {
        return localUserRepository.observeUsers()
    }

    suspend fun getUser(userId: String): User? {
        return localUserRepository.getUserById(userId)
    }

    suspend fun hardRefreshUsersData() {
        val res = remoteUserRepository.hardRefreshUsers()
        if(res is Success){
            Log.d(TAG,"Refreshing users to local..")
            val users = res.data
            Log.d(TAG,"users: ${users.size}")
            localUserRepository.insertMultiUsers(users)
        }
    }

    suspend fun observeAndRefreshUser(userId: String) {
        remoteUserRepository.observeRemoteUser(userId){
           runBlocking {
               localUserRepository.updateUser(it)
               sessionManager.update(it)
               Log.d(TAG,"user is updated in local")
           }
        }
    }

    fun isRememberMeOn(): Boolean {
        return sessionManager.isRememberMeOn()
    }

    suspend fun getAllUsers(): List<User>? {
         return localUserRepository.getUsersData()
    }


}