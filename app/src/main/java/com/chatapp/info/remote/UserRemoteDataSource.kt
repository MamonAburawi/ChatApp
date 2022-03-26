package com.chatapp.info.remote

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.local.user.UserDataSource
import com.chatapp.info.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.tasks.await

class UserRemoteDataSource(): UserDataSource {

    private val observableUser = MutableLiveData<Result<User>?>()
    private val _root = FirebaseFirestore.getInstance()
    private fun usersCollection() = _root.collection(USERS_COLLECTION)


    override suspend fun addUser(user: User) {
        usersCollection().document(user.userId).set(user)
            .addOnSuccessListener {
                Log.d(TAG,"user is added")
            }
            .addOnFailureListener {error ->
                Log.d(TAG, "firebase fire store error occurred: ${error.message}")
            }
    }

    override suspend fun updateUser(user: User) {
        usersCollection().document(user.userId).update(user.toHashMap())
            .addOnSuccessListener {
                Log.d(TAG,"user is updated")
            }
            .addOnFailureListener {error ->
                Log.d(TAG, "firebase fire store error occurred: ${error.message}")
            }
    }

    override suspend fun deleteAllUsers() {

    }

    override suspend fun deleteUserById(userId: String) {
        usersCollection().document(userId).delete()
            .addOnSuccessListener {
                Log.d(TAG,"user is removed")
            }
            .addOnFailureListener {

            }
    }

    override suspend fun getUserById(userId: String): User? =
            usersCollection().whereEqualTo(USER_ID_FIELD, userId).get().await()
                .toObjects(User::class.java)[0]


    override suspend fun hardRefreshUsers(): Result<List<User>> {
        return try {
            val users = usersCollection().get().await().toObjects(User::class.java)
            Log.d(TAG,"users in remote: ${users.size}")
            Result.Success(users)
        }catch (ex: Exception){
            Result.Error(ex)
        }
    }


    override suspend fun getUsersData(): List<User>? {
        return emptyList()
    }

    override suspend fun insertChat(chat: User.Chat) {
        val senderRef = usersCollection().whereEqualTo(USER_ID_FIELD, chat.senderId).get().await()
        if (!senderRef.isEmpty) {
            val docId = senderRef.documents[0].id
            usersCollection().document(docId)
                .update(CHATS, FieldValue.arrayUnion(chat))

        }

        val recipientRef = usersCollection().whereEqualTo(USER_ID_FIELD, chat.recipientId).get().await()
        if (!recipientRef.isEmpty) {
            val docId = recipientRef.documents[0].id
            usersCollection().document(docId)
                .update(CHATS, FieldValue.arrayUnion(chat))

        }
    }

    override suspend fun deleteChat(chat: User.Chat,forBoth: Boolean) {
        val userRef = usersCollection().whereEqualTo(USER_ID_FIELD, chat.senderId).get().await()
        if (!userRef.isEmpty) {
            val docId = userRef.documents[0].id
            usersCollection().document(docId)
                .update(CHATS, FieldValue.arrayRemove(chat))
        }
        if (forBoth){
            val recipientRef = usersCollection().whereEqualTo(USER_ID_FIELD, chat.recipientId).get().await()
            if (!recipientRef.isEmpty) {
                val docId = recipientRef.documents[0].id
                usersCollection().document(docId)
                    .update(CHATS, FieldValue.arrayRemove(chat))
            }

        }
    }

    override suspend fun updateChat(chat: List<User.Chat>) {
        val senderRef = usersCollection().whereEqualTo(USER_ID_FIELD, chat[0].senderId).get().await()
        if (!senderRef.isEmpty) {
            val docId = senderRef.documents[0].id
            usersCollection().document(docId)
                .update(CHATS, chat)

        }

        val recipientRef = usersCollection().whereEqualTo(USER_ID_FIELD, chat[0].recipientId).get().await()
        if (!recipientRef.isEmpty) {
            val docId = recipientRef.documents[0].id

            usersCollection().document(docId)
                .update(CHATS, chat)

        }
    }

    override fun observeLocalUser(userId: String): LiveData<Result<User>?> {
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


    override suspend fun observeRemoteUser(userId: String,update:(User)-> Unit) {
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