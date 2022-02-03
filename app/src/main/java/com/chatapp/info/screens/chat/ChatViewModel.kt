package com.chatapp.info.screens.chat

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatapp.info.MessageState
import com.chatapp.info.data.Chat
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.genUUID
import com.chatapp.info.repository.server.ChatRepositoryOnline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class ChatViewModel(application: Application,val user: User) : AndroidViewModel(application) {

    companion object{
        const val TAG = "Chat"
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

    private var list = ArrayList<Message>()



    private var chatId = ""
    private var currentMessage: Message? = null

    init {
        _progress.value = false
        _isMessageExist.value = false

        createNewChatId { id->
            chatId = id
        }


    }


//    fun insert(message: Message){
//        list.add(message)
//        _messages.value = list
//    }

    fun resetMessageState(){
        _messageState.value = null
    }



    fun isMessageSent(message: Message,list: List<Message>){
      
    }

    fun addMessage(message: Message){
        _isMessageExist.value = false
        scopeIO.launch {
            try {
                chatsPath.document(chatId).collection("messages").document(message.id.toString())
                    .set(message)
                    .addOnCompleteListener {
                        Log.i(TAG,"message is inserted")
                        _messageState.value = MessageState.INSERT

                    }.addOnFailureListener {
                        Log.i(TAG,it.message.toString())
                    }

            }catch(ex:Exception){
                Log.i(TAG,ex.message.toString())
            }
        }
    }


    fun removeMessage(message: Message){
        scopeIO.launch {
            try {
                chatsPath.document(chatId)
                    .collection("messages")
                    .document(message.id.toString())
                    .delete()
                    .addOnSuccessListener {
                        chatsPath.document().collection("messages").document(message.id.toString()).get()
                            .addOnSuccessListener {
                                if (it.exists()){}
                                else{
                                    Log.i(TAG,"message is removed")
                                    _messageState.value = MessageState.DELETE

                                }
                            }
                    }
                    .addOnFailureListener {  Log.i(TAG,it.message.toString())}
            }catch (ex: Exception){
                Log.i(TAG,ex.message.toString())
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