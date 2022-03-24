package com.chatapp.info.remote

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chatapp.info.data.Message
import com.chatapp.info.local.message.MessageDataSource
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

class MessageRemoteDataSource(): MessageDataSource {

    private val observableMessages = MutableLiveData<Result<List<Message>>?>()
    private val _rootFireStore = FirebaseFirestore.getInstance()

    private val _rootStorage = FirebaseStorage.getInstance()
    private fun storageRef() = _rootStorage.reference

    private fun chatCollection() = _rootFireStore.collection(CHAT_COLLECTION)
    private fun messagesCollection(chatId: String) = chatCollection().document(chatId).collection(MESSAGES_COLLECTION)

    override fun observeMessages(): LiveData<Result<List<Message>>?> {
        return observableMessages

    }

    override fun observeMessagesByChatId(chatId: String): LiveData<Result<List<Message>>?> {
        return observableMessages
    }

    fun observeMessagesOnRemoteByChatId(chatId: String,listener:(List<Message>) -> Unit) {
        messagesCollection(chatId).addSnapshotListener { value, error ->
            if (error == null){
                if (value != null){
                    val messages = value.toObjects(Message::class.java)
                    listener(messages)
                }else{
                    listener(emptyList())
                }
            }else{
                listener(emptyList())
            }
        }

    }


    override suspend fun getMessageByChatId(chatId: String): Result<List<Message>> {
        val resRef = messagesCollection(chatId).get().await()
        return if (!resRef.isEmpty) {
            Success(resRef.toObjects(Message::class.java))
        } else {
            Error(Exception("Error getting Messages!"))
        }
    }

    override suspend fun getMessageById(messageId: String): Result<Message?> {
        val resRef = messagesCollection(messageId).whereEqualTo(MESSAGE_ID_FIELD, messageId).get().await()
        return if (!resRef.isEmpty) {
            Success(resRef.toObjects(Message::class.java)[0])
        } else {
            Error(Exception("message with id: {$messageId} Not Found!"))
        }
    }

    override suspend fun insertMessage(message: Message) {
        Log.d(TAG,"Inserting Message")
//        chatCollection().add(message)
        chatCollection()
            .document(message.chatId)
            .collection(MESSAGES_COLLECTION)
            .document(message.messageId)
            .set(message.toHashMap())
            .addOnSuccessListener {
            Log.d(TAG,"Message is added successfully")
            }
            .addOnFailureListener {
                Log.d(TAG,"Adding Message is Failed -> ${it.message}")
            }
    }

    override suspend fun updateMessage(message: Message) {
        val resRef = messagesCollection(message.chatId).whereEqualTo(MESSAGE_ID_FIELD, message.messageId).get().await()
        if (!resRef.isEmpty) {
            val docId = resRef.documents[0].id
            messagesCollection(message.chatId).document(docId).set(message.toHashMap()).await()
        } else {
            Log.d(TAG, "onUpdateMessage: product with id: ${message.messageId} not found!")
        }
    }

    override suspend fun deleteMessage(message: Message) {
        Log.d(TAG, "onDeleteMessage: delete message with Id: ${message.messageId} initiated")
        val resRef = messagesCollection(message.chatId).whereEqualTo(MESSAGE_ID_FIELD, message.messageId).get().await()
        if (!resRef.isEmpty) {
            val m = resRef.documents[0].toObject(Message::class.java)
            val imgUrl = m?.images

            //deleting image first
            imgUrl?.forEach { image->
                deleteImage(image)
            }


            //deleting doc containing Message
            val docId = resRef.documents[0].id
            messagesCollection(m!!.chatId).document(docId).delete()
                .addOnSuccessListener {
                Log.d(TAG, "onDelete: DocumentSnapshot successfully deleted!")
            }.addOnFailureListener { e ->
                Log.w(TAG, "onDelete: Error deleting document", e)
            }
        } else {
            Log.d(TAG, "onDeleteMessage: message with id: ${message.messageId} not found!")
        }
    }



    override suspend fun deleteAllMessagesByChatId(chatId: String) {
        chatCollection().document(chatId).delete()
            .addOnSuccessListener {
                Log.d(TAG,"onDelete: Collection is Removed")
            }
            .addOnFailureListener {
                Log.w(TAG,"onDelete: chatId {$chatId} is not removed! ")
            }
    }

    override suspend fun uploadImage(uri: Uri, fileName: String): Uri? {
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

    override fun deleteImage(imgUrl: String) {
        val ref = Firebase.storage.getReferenceFromUrl(imgUrl)
        ref.delete().addOnSuccessListener {
            Log.d(TAG, "onDelete: image deleted successfully!")
        }.addOnFailureListener { e ->
            Log.d(TAG, "onDelete: Error deleting image, error: $e")
        }
    }

    override fun revertUpload(fileName: String) {
        val imgRef = storageRef().child("$CHATS_STORAGE/$fileName")
        imgRef.delete().addOnSuccessListener {
            Log.d(TAG, "onRevert: File with name: $fileName deleted successfully!")
        }.addOnFailureListener { e ->
            Log.d(TAG, "onRevert: Error deleting file with name = $fileName, error: $e")
        }
    }





    companion object {

        private const val CHAT_COLLECTION = "chats"
        private const val MESSAGES_COLLECTION = "messages"
        private const val MESSAGE_ID_FIELD = "messageId"
        private const val CHAT_ID_FIELD = "chatId"
        private const val SENDER_ID_FIELD = "senderId"
        private const val RECIPIENT_ID_FIELD = "recipientId"
        private const val DATE_FIELD = "date"
        const val CHATS_STORAGE = "Chats"

        private const val TAG = "MessageRemoteDataSource"
    }

}