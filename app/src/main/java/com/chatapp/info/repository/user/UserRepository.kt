package com.chatapp.info.repository.user

import android.util.Log
import androidx.lifecycle.LiveData
import com.chatapp.info.data.User
import com.chatapp.info.local.user.UserDataSource
import com.chatapp.info.remote.UserRemoteDataSource
import com.chatapp.info.utils.ChatAppSessionManager
import com.chatapp.info.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import kotlinx.coroutines.*


class UserRepository(
    private val userLocalDataSource: UserDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val sessionManager: ChatAppSessionManager
) : UserRepoInterface {

    companion object {
        private const val TAG = "UserRepository"
    }

    private var firebaseAuth = FirebaseAuth.getInstance()
//    private val userId = sessionManager.getUserIdFromSession()


    override suspend fun insertMultiUsers(users: List<User>) {
        userLocalDataSource.insertMultiUsers(users)
    }


    override suspend fun signUp(user: User) {
        sessionManager.createLoginSession(user.userId, user.name, false)
        Log.d(TAG, "on SignUp: Updating user in Local Source")
        userLocalDataSource.addUser(user)
        Log.d(TAG, "on SignUp: Updating userdata on Remote Source")
        userRemoteDataSource.addUser(user)
    }

    override suspend fun login(user: User, rememberMe: Boolean) {
        sessionManager.createLoginSession(
            user.userId,
            user.name,
            rememberMe
        )
        userLocalDataSource.addUser(user)
    }

    override suspend fun signOut() {
		sessionManager.logoutFromSession()
		userLocalDataSource.deleteAllUsers()
        firebaseAuth.signOut()
    }

    override suspend fun deleteUser(userId: String) {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onDelete: deleting User from remote source")
                userRemoteDataSource.deleteUserById(userId)
            }
            val localRes = async {
                Log.d(TAG, "onDelete: deleting User from local source")
                userLocalDataSource.deleteUserById(userId)
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


    override fun observeLocalUser(userId: String): LiveData<Result<User>?> {
        return userLocalDataSource.observeLocalUser(userId)
    }


    override suspend fun getUser(userId: String): User? {
        return userLocalDataSource.getUserById(userId)
    }

    override suspend fun hardRefreshUsersData() {
        val res = userRemoteDataSource.hardRefreshUsers()
        if(res is Success){
            Log.d(TAG,"Refreshing users to local..")
            val users = res.data
            Log.d(TAG,"users: ${users.size}")
            userLocalDataSource.insertMultiUsers(users)
        }
//        userLocalDataSource.deleteAllUsers()



//        if (userId != null) {
//            userLocalDataSource.deleteUserById(userId)
//            val user = userRemoteDataSource.getUserById(userId)
//            if (user != null){
//                userLocalDataSource.addUser(user)
//            }
//        }

    }

    override suspend fun observeAndRefreshUser(userId: String) {
        userRemoteDataSource.observeRemoteUser(userId){
           runBlocking {
               userLocalDataSource.updateUser(it)
               sessionManager.update(it)
               Log.d(TAG,"user is updated in local")
           }
        }
    }



//    override suspend fun insertChatId(chatId: String): Result<Boolean>{
//        return supervisorScope {
//            val localRes = async {
//                Log.d(TAG, "onInsertChatId: adding ChatId to local source")
//                userLocalDataSource.insertChatId(userId!!,chatId)
//            }
//            val remoteRes = async {
//                userRemoteDataSource.insertChatId(userId!!,chatId)
//            }
//            try {
//                localRes.await()
//                remoteRes.await()
//                Success(true)
//            } catch (e: Exception) {
//                Error(e)
//            }
//        }
//    }

    override suspend fun insertChat(chat: User.Chat) {
        return supervisorScope {

            val localRes = async {
                Log.d(TAG, "onInsertChat: adding Chat to local source")
                userLocalDataSource.insertChat(chat)
            }
            val remoteRes = async {
                Log.d(TAG, "onInsertChat: adding Chat to remote source")
                userRemoteDataSource.insertChat(chat)
            }
            try {
                localRes.await()
                remoteRes.await()
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    override suspend fun deleteChat(chat: User.Chat) {
        return supervisorScope {
            val localRes = async {
                Log.d(TAG, "onDeleteChat: deleting Chat from local source")
                userLocalDataSource.deleteChat(chat)
            }
            val remoteRes = async {
                Log.d(TAG, "onDeleteChat: deleting Chat from remote source")
                userRemoteDataSource.deleteChat(chat)
            }
            try {
                localRes.await()
                remoteRes.await()
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }


    override suspend fun updateChat(chat: List<User.Chat>) {
        return supervisorScope {

            val remoteRes = async {
                Log.d(TAG, "onUpdateChat: updating Chat to remote source")
                userRemoteDataSource.updateChat(chat)
            }
            try {
                remoteRes.await()
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }


    override fun isRememberMeOn(): Boolean {
        return sessionManager.isRememberMeOn()
    }


    override suspend fun getAllUsers(): List<User>? {
         return userLocalDataSource.getUsersData()
    }


}