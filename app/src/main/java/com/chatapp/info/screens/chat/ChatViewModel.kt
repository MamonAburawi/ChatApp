package com.chatapp.info.screens.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*
import com.chatapp.info.MessageState
import com.chatapp.info.data.Chat
import com.chatapp.info.data.DateConverter
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.genUUID
import com.chatapp.info.repository.server.ChatRepositoryOnline
import com.chatapp.info.workmanager.RemoveMessage
import com.chatapp.info.workmanager.SendMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class ChatViewModel(application: Application,val user: User) : AndroidViewModel(application) {

    companion object{
        const val TAG = "Chat"
    }

    val worker by lazy {
        WorkManager.getInstance(application)
    }

    private val sendMessageWorker by lazy {
        OneTimeWorkRequest.Builder(SendMessage::class.java)
    }
    private val removeMessageWorker by lazy {
        OneTimeWorkRequest.Builder(RemoveMessage::class.java)
    }

    private val scopeIO = CoroutineScope(Dispatchers.IO + Job())
    private val scopeMain = CoroutineScope(Dispatchers.Main + Job())

    private val repositoryOnline = ChatRepositoryOnline()

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _progress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = _progress

    private val _messageState = MutableLiveData<String?>()
    val messageState: LiveData<String?> = _messageState

    private val _isMessageExist = MutableLiveData<Boolean>()
    val isMessageExist: LiveData<Boolean> = _isMessageExist

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // firebase fire store
    private val _root = FirebaseFirestore.getInstance()
    private val chatsPath = _root.collection("chats")

    // firebase authentication
    private val _auth = FirebaseAuth.getInstance()


    /** worker ids **/
    private val _sendMessageWorkerId = MutableLiveData<UUID>()
    val sendMessageWorkerId: LiveData<UUID> = _sendMessageWorkerId

    private val _removeMessageWorkerId = MutableLiveData<UUID>()
    val removeMessageWorkerId: LiveData<UUID> = _removeMessageWorkerId


    private var chatId = ""
    private var currentMessage: Message? = null

    init {
        _progress.value = false
        _isMessageExist.value = false

        createNewChatId { id->
            chatId = id
        }


    }


    fun resetMessageState(){
        _messageState.value = null
    }




    fun addMessage(message: Message){
        _isMessageExist.value = false

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
            data.putString("image",message.image)
            data.putString("type",message.type)
            data.putString("chat_id",chatId)

            sendMessageWorker.setInputData(data.build())
            sendMessageWorker.setConstraints(constraints)

            val builder = sendMessageWorker.build()

            worker.beginWith(builder).enqueue()

            withContext(Dispatchers.Main){
                _sendMessageWorkerId.value = builder.id
            }

//            Log.i("worker","sender message worker id: ${builder.id}")

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


            removeMessageWorker.setInputData(data.build())
            removeMessageWorker.setConstraints(constraints)

            val builder = removeMessageWorker.build()

            worker.beginWith(builder).enqueue()

            withContext(Dispatchers.Main){
                _removeMessageWorkerId.value = builder.id
            }

//            try {
//                chatsPath.document(chatId)
//                    .collection("messages")
//                    .document(message.id.toString())
//                    .delete()
//                    .addOnSuccessListener {
//                        chatsPath.document().collection("messages").document(message.id.toString()).get()
//                            .addOnSuccessListener {
//                                if (it.exists()){}
//                                else{
//                                    Log.i(TAG,"message is removed")
//                                    _messageState.value = MessageState.DELETE
//
//                                }
//                            }
//                    }
//                    .addOnFailureListener {  Log.i(TAG,it.message.toString())}
//            }catch (ex: Exception){
//                Log.i(TAG,ex.message.toString())
//            }
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