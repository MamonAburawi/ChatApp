package com.chatapp.info.repository.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chatapp.info.data.Chat
import com.chatapp.info.data.User
import com.chatapp.info.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.tasks.await

class RemoteUserRepository() {

    private val observableUser = MutableLiveData<Result<User>?>()
    private val observableUsers = MutableLiveData<Result<List<User>?>>()

    private val _root = FirebaseFirestore.getInstance()
    private fun usersCollection() = _root.collection(USERS_COLLECTION)

    private fun chatCollection(userId: String) = usersCollection().document(userId)
        .collection(CHAT_COLLECTION)

    suspend fun addUser(user: User) {
        usersCollection().document(user.userId).set(user)
            .addOnSuccessListener {
                Log.d(TAG,"user is added")
            }
            .addOnFailureListener { error ->
                Log.d(TAG, "firebase fire store error occurred: ${error.message}")
            }
    }

    suspend fun updateUser(user: User) {
        usersCollection().document(user.userId).update(user.toHashMap())
            .addOnSuccessListener {
                Log.d(TAG,"user is updated")
            }
            .addOnFailureListener {error ->
                Log.d(TAG, "firebase fire store error occurred: ${error.message}")
            }
    }

    suspend fun deleteAllUsers() {

    }

    suspend fun getUserChats(userId: String): Result<List<Chat>> {
        return try {
            val ref = chatCollection(userId).get().await()
            val chats = ref.toObjects(Chat::class.java)
            Result.Success(chats)
        }catch (ex: Exception){
            Result.Error(ex)
        }

    }

    suspend fun observeUserChatsIds(userId: String, ChatsIds: (List<Chat>) -> Unit) {
        chatCollection(userId).addSnapshotListener { value, error ->
            if (error == null){
                if (value != null){
                    val chats = value.toObjects(Chat::class.java)
                    ChatsIds(chats)
                }
            }
        }
    }

    suspend fun refreshUsersByIds(ids: List<String>) {
        ids.forEach { userId ->
            val ref = usersCollection().document(userId).get().await()
            val user = ref.toObject(User::class.java)
            if (user != null) {
                updateUser(user)
            }
        }
    }

    fun observeUsers(): LiveData<Result<List<User>?>> {
        return observableUsers
    }

    suspend fun deleteUserById(userId: String) {
        usersCollection().document(userId).delete()
            .addOnSuccessListener {
                Log.d(TAG,"user is removed")
            }
            .addOnFailureListener {

            }
    }

    suspend fun getUserById(userId: String): User? = usersCollection().whereEqualTo(USER_ID_FIELD, userId).get().await()
                .toObjects(User::class.java)[0]

    suspend fun hardRefreshUsers(): Result<List<User>> {
        return try {
            val users = usersCollection().get().await().toObjects(User::class.java)
            Log.d(TAG,"users in remote: ${users.size}")
            Result.Success(users)
        }catch (ex: Exception){
            Result.Error(ex)
        }
    }

    suspend fun getUsersData(): List<User>? {
        return emptyList()
    }

    fun observeLocalUser(userId: String): LiveData<Result<User>?> {
        return observableUser
    }

    suspend fun checkPassByUserId(userId: String ,password: String,onComplete: (Boolean) -> Unit) {
        val ref = usersCollection().whereEqualTo(USER_ID_FIELD,userId).get().await()
        if (ref != null){
            val user = ref.toObjects(User::class.java)[0]
            Log.i("Login","email: ${user.email}")
            if (user.password == password){
                onComplete(true)
            }else{
                onComplete(false)
            }
        }
    }

    suspend fun observeRemoteUser(userId: String,update:(User)-> Unit) {
       usersCollection().whereEqualTo(USER_ID_FIELD,userId).addSnapshotListener { value, error ->
            if (error == null){
                if (value != null){
                    val user = value.toObjects(User::class.java)[0]
                    update(user)
                }
            }
        }
    }

    suspend fun checkUserIsExist(email: String, isExist:(Boolean) -> Unit, onError:(String) -> Unit) {
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email).addOnCompleteListener {
            try {
                val isUserExist = it.result.signInMethods?.isEmpty()
                if (isUserExist!!){ // user is not exist
                    isExist(false)
                }else{ // user is exist
                    isExist(true)
                }
            }
            catch (ex: Exception){ // maybe there is another error..
                onError("Connection is not found!")
            }
        }
    }


    companion object {

        private const val USERS_COLLECTION = "users"
        private const val USER_ID_FIELD = "userId"
        private const val CHAT_COLLECTION = "chats"
        private const val EMAIL_FIELD = "email"
        private const val PHONE_FIELD = "phone"
        private const val PASSWORD_FIELD = "password"
        private const val LAST_ONLINE_FIELD = "lastOnline"
        private const val DATE_FIELD = "date"
        private const val USERS_FIELD = "users"
        private const val CHAT_IDs = "chatIds"
        private const val CHATS = "chats"

        private const val TAG = "UserRemoteDataSource"
    }

}