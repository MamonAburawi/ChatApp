package com.chatapp.info.repository.chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chatapp.info.data.Chat
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class RemoteChatRepository() {

    private val observableChats = MutableLiveData<Result<List<Chat>>?>()
    private val _rootFireStore = FirebaseFirestore.getInstance()

    private val _rootStorage = FirebaseStorage.getInstance()
    private fun storageRef() = _rootStorage.reference

    private fun usersCollection() = _rootFireStore.collection(USERS_COLLECTION)
    private fun chatCollection(userId: String) = usersCollection().document(userId)
        .collection(CHAT_COLLECTION)

    fun observeChats(): LiveData<Result<List<Chat>>?> {
        return observableChats
    }

    suspend fun getChats(userId: String): Result<List<Chat>?> {
        return try {
            val ref = chatCollection(userId).get().await()
            val chats = ref.toObjects(Chat::class.java)
            Result.Success(chats)
        }catch (ex: Exception){
            Result.Error(ex)
        }
    }


    fun observeChatsOnRemoteByChatId(userId: String,listener:(List<Chat>) -> Unit) {
        chatCollection(userId).addSnapshotListener { value, error ->
            if (error == null){
                if (value != null){
                    val chats = value.toObjects(Chat::class.java)
                    listener(chats)
                }else{
                    listener(emptyList())
                }
            }else{
                listener(emptyList())
            }
        }
    }

    suspend fun getChatById(chatId: String): Result<Chat?> {
        val resRef = chatCollection(chatId).whereEqualTo(CHAT_ID_FIELD, chatId).get().await()
        return if (!resRef.isEmpty) {
            Success(resRef.toObjects(Chat::class.java)[0])
        } else {
            Error(Exception("message with id: {$chatId} Not Found!"))
        }
    }

     suspend fun insertChat(chat: Chat) {
        Log.d(TAG,"Inserting Chat to remote")


//        usersCollection().document(chat.senderId)
//            .update(CHAT_COLLECTION,FieldValue.arrayUnion(chat.chatId.toHashSet()))

        chatCollection(chat.senderId)
            .document(chat.chatId)
            .set(chat)

//        usersCollection().document(chat.recipientId)
//            .update(CHAT_COLLECTION,FieldValue.arrayUnion(chat.chatId.toHashSet()))

        chatCollection(chat.recipientId)
            .document(chat.chatId)
            .set(chat)



    }

     suspend fun updateChat(chat: Chat) {
        val senderRef = chatCollection(chat.senderId).document(chat.chatId)
        val recipientRef = chatCollection(chat.recipientId).document(chat.chatId)

        val senderData = senderRef.get().await()
        if (senderData.exists()){
            senderRef.update(chat.toHashMap())
            Log.d(TAG, "onUpdateChat: chat is updated in sender")
        }else{
            Log.d(TAG, "onUpdateChat: chat with id: ${chat.chatId} is not exist in sender")
        }

        val recipientData = recipientRef.get().await()
        if (recipientData.exists()){
            recipientRef.update(chat.toHashMap())
            Log.d(TAG, "onUpdateChat: chat is updated in recipient")
        }else{
            Log.d(TAG, "onUpdateChat: chat with id: ${chat.chatId} is not exist in recipient")
        }

    }

     suspend fun deleteChat(chat: Chat) {
        Log.d(TAG, "onDeleteChat: delete chat with Id: ${chat.chatId} initiated")
        val resRef = chatCollection(chat.senderId).document(chat.chatId)
        val c = resRef.get().await()
        if (c.exists()) {
            val ch = c.toObject(Chat::class.java)
            val imgUrl = ch?.image

            //deleting image first
            if (imgUrl != null) {
                deleteImage(imgUrl)
            }

            //deleting doc containing Message
            resRef.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "onDelete: DocumentSnapshot successfully deleted!")
            }
                .addOnFailureListener {
                    Log.w(TAG, "onDelete: Error deleting document:${it.message}")
            }

        } else {
            Log.d(TAG, "onDeleteMessage: Chat not found!")
        }
    }


     suspend fun uploadImage(uri: Uri, fileName: String): Uri? {
        val imgRef = storageRef().child("$CHATS_STORAGE/$fileName")
        val uploadTask = imgRef.putFile(uri)
        val uriRef = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            imgRef.downloadUrl
        }
        return uriRef.await()
    }

     fun deleteImage(imgUrl: String) {
        val ref = Firebase.storage.getReferenceFromUrl(imgUrl)
        ref.delete().addOnSuccessListener {
            Log.d(TAG, "onDelete: image deleted successfully!")
        }.addOnFailureListener { e ->
            Log.d(TAG, "onDelete: Error deleting image, error: $e")
        }
    }

     fun revertUpload(fileName: String) {
        val imgRef = storageRef().child("$CHATS_STORAGE/$fileName")
        imgRef.delete().addOnSuccessListener {
            Log.d(TAG, "onRevert: File with name: $fileName deleted successfully!")
        }.addOnFailureListener { e ->
            Log.d(TAG, "onRevert: Error deleting file with name = $fileName, error: $e")
        }
    }





    companion object {

        private const val USERS_COLLECTION = "users"
        private const val CHAT_ID_FIELD = "chatId"
        private const val CHAT_COLLECTION = "chats"
        const val CHATS_STORAGE = "Chats"

        private const val TAG = "MessageRemoteDataSource"
    }

}