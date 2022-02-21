package com.chatapp.info.screens.chat

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.chatapp.info.data.Chat
import com.chatapp.info.data.DateConverter
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.genUUID
import com.chatapp.info.repository.server.ChatRepositoryOnline
import com.chatapp.info.workmanager.RemoveMessageWork
import com.chatapp.info.workmanager.SendMessageWork
import com.chatapp.info.workmanager.UploadImageWork
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.*
import java.util.*

class ChatViewModel(application: Application,val user: User) : AndroidViewModel(application) {

    companion object{
        const val TAG = "Chat"
    }

    private val worker by lazy {
        WorkManager.getInstance(application)
    }

    private val sendMessageWorker by lazy {
        OneTimeWorkRequest.Builder(SendMessageWork::class.java)
    }
    private val removeMessageWorker by lazy {
        OneTimeWorkRequest.Builder(RemoveMessageWork::class.java)
    }
    private val uploadImageWorker by lazy{
        OneTimeWorkRequest.Builder(UploadImageWork::class.java)
    }
    private val scopeIO = CoroutineScope(Dispatchers.IO + Job())
    private val scopeMain = CoroutineScope(Dispatchers.Main + Job())

    private val repositoryOnline = ChatRepositoryOnline()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _progress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = _progress

    private val _isSending = MutableLiveData<Boolean?>()
    val isSending: LiveData<Boolean?> = _isSending

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // firebase fire store
    private val _root = FirebaseFirestore.getInstance()
    private val chatsPath = _root.collection("chats")

    // firebase authentication
    private val _auth = FirebaseAuth.getInstance()


// TODO: add function to check if the connection is still or not and put it inside live data

    /** worker ids **/
    private val _sendMessageWorkerId = MutableLiveData<UUID>()
    val sendMessageWorkerId: LiveData<UUID> = _sendMessageWorkerId

    private val _removeMessageWorkerId = MutableLiveData<UUID>()
    val removeMessageWorkerId: LiveData<UUID> = _removeMessageWorkerId

    private val _uploadImageWorkerId = MutableLiveData<UUID>()
    val uploadImageWorkerId: LiveData<UUID> = _uploadImageWorkerId


    private var chatId = ""

    init {
        _progress.value = false
        _isSending.value = null

        createNewChatId { id->
            chatId = id
        }


    }



    fun sendingComplete(){
        _isSending.value = false
    }


    fun addMessage(message: Message,image: Uri? = null){

        scopeIO.launch {

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val dateToLong = DateConverter.to(message.time)

            val data = Data.Builder()
            data.putLong("message_id",message.id)
            data.putString("text",message.text)
            data.putLong("date", dateToLong!!)
            data.putString("sender_id",message.senderId)
            data.putString("recipient_id",message.recipientId)
            data.putString("image_id",message.imageId)
            data.putString("type",message.type)
            data.putString("chat_id",chatId)
            data.putString("image", image.toString())

            uploadImageWorker
                .setInputData(data.build())
                .setConstraints(constraints)

            sendMessageWorker
                .setInputData(data.build())
                .setConstraints(constraints)

            val uploadImageBuilder = uploadImageWorker.build()
            val sendMessageBuilder = sendMessageWorker.build()

            if (image != null){ // image it will uploader then message it will be sent
                worker.beginWith(uploadImageBuilder)
                    .then(sendMessageBuilder)
                    .enqueue()

                withContext(Dispatchers.Main){
                    _isSending.value = true
                }
            }else{
                worker.beginWith(sendMessageBuilder).enqueue()
            }

            withContext(Dispatchers.Main){
                _uploadImageWorkerId.value = uploadImageBuilder.id
                _sendMessageWorkerId.value = sendMessageBuilder.id
            }


        }

    }

    fun removeMessage(message: Message){
        scopeIO.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val data = Data.Builder()
            data.putString("chat_id",chatId)
            data.putString("message_id",message.id.toString())

            removeMessageWorker
                .setInputData(data.build())
                .setConstraints(constraints)

            val builder = removeMessageWorker.build()

            worker.beginWith(builder).enqueue()

            withContext(Dispatchers.Main){
                _removeMessageWorkerId.value = builder.id
            }
        }
    }


    private fun createNewChatId(chatId:(String) -> Unit) {
        val newChatId = genUUID()
        scopeIO.launch {
            _root.collection("users")
                .document(_auth.currentUser!!.uid)
                .collection("chats")
                .document(user.id).get().addOnSuccessListener {
                    if (it.exists()){
                        val chat = it.toObject(Chat::class.java)
                        Log.i(TAG,"old chat id: ${chat!!.chatId}")
                        chatId(chat.chatId) // get chat id
                        getMessages(chat.chatId)
                    }else{
                        chatId(newChatId) // create new chat id
                        getMessages(newChatId)
                        scopeIO.launch {
                            val senderId = _auth.currentUser!!.uid
                            val chat = Chat(newChatId,senderId,user.id,Calendar.getInstance().time)

                            _root.collection("users")
                                .document(senderId)
                                .collection("chats")
                                .document(user.id)
                                .set(chat)

                            _root.collection("users")
                                .document(user.id)
                                .collection("chats")
                                .document(senderId)
                                .set(chat)
                        }
                    }
                }
        }
    }




    private fun getMessages(chatId: String?){
        scopeIO.launch {
            if (chatId != null){
                repositoryOnline.getMessages(chatsPath.document(chatId).collection("messages")).orderBy("time")
                    .addSnapshotListener { value, error ->
                        if (error == null){
                            if (value != null){
                                val messages = value.toObjects(Message::class.java) // here you will not receive the message each time when the data is updated
                                _messages.value = messages

                            }
                        }
                    }
            }else{
                Log.i(TAG,"chat id is not found!")
            }
        }
    }







    override fun onCleared() {
        super.onCleared()

        scopeIO.cancel()
        scopeMain.cancel()
    }
}