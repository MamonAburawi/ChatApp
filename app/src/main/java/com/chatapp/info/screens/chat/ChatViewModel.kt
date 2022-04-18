package com.chatapp.info.screens.chat

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.work.*

import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.utils.*
import com.chatapp.info.workmanager.RemoveMessageWork
import com.chatapp.info.workmanager.SendMessageWork
import com.chatapp.info.workmanager.UploadImageWork
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import com.chatapp.info.data.Chat
import com.chatapp.info.repository.chat.ChatRepository
import com.chatapp.info.repository.message.MessageRepository
import com.chatapp.info.repository.user.UserRepository
import kotlinx.coroutines.*
import java.util.*


class ChatViewModel(
     val chatRepository: ChatRepository,
     val userRepository: UserRepository,
     val messageRepository: MessageRepository): ViewModel() {

    private val userId = userRepository.sessionManager.getUserIdFromSession()

    companion object{
        const val TAG = "ChatViewModel"
    }

//    private val worker by lazy { WorkManager.getInstance(application) }
//
//    private val sessionManager by lazy { ChatAppSessionManager(application) }
//    private val chatApplication by lazy { ChatApplication(application) }

//    private val messageRepository by lazy { chatApplication.messageRepository }
//    private val userRepository by lazy { chatApplication.userRepository }
//    private val chatRepository by lazy { chatApplication.chatRepository }

//    private val userId = sessionManager.getUserIdFromSession()

    private val sendMessageWorker by lazy {
        OneTimeWorkRequest.Builder(SendMessageWork::class.java)
    }
    private val removeMessageWorker by lazy {
        OneTimeWorkRequest.Builder(RemoveMessageWork::class.java)
    }
    private val uploadImageWorker by lazy {
        OneTimeWorkRequest.Builder(UploadImageWork::class.java)
    }



    private var _chatMessages = MutableLiveData<List<Message>>()
    val chatMessages: LiveData<List<Message>> get() = _chatMessages


    private var _chats = MutableLiveData<List<Chat>>()
    val chats: LiveData<List<Chat>> get() = _chats


    private var _allMessages = MutableLiveData<List<Message>>()
    val allMessages: LiveData<List<Message>> get() = _allMessages


    private var _newMessages = MutableLiveData<List<Message>>()
    val newMessages: LiveData<List<Message>> = _newMessages


    private val _storeDataStatus = MutableLiveData<StoreDataStatus>()
    val storeDataStatus: LiveData<StoreDataStatus> get() = _storeDataStatus


    private val _isSending = MutableLiveData<Boolean?>()
    val isSending: LiveData<Boolean?> = _isSending

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

//    val recipientU = MutableLiveData<User>()
    val chatId = MutableLiveData<String>()




// TODO: add function to check if the connection is still or not and put it inside live data

    /** worker ids **/
    private val _sendMessageWorkerId = MutableLiveData<UUID>()
    val sendMessageWorkerId: LiveData<UUID> = _sendMessageWorkerId

    private val _removeMessageWorkerId = MutableLiveData<UUID>()
    val removeMessageWorkerId: LiveData<UUID> = _removeMessageWorkerId

    private val _uploadImageWorkerId = MutableLiveData<UUID>()
    val uploadImageWorkerId: LiveData<UUID> = _uploadImageWorkerId


    // TODO: make chat status live data.


    private fun getMessagesLiveData(result: Result<List<Message>?>?): LiveData<List<Message>> {
        val res = MutableLiveData<List<Message>>()
        if (result is Success) {
            Log.d(TAG, "result is success")
            _storeDataStatus.value = StoreDataStatus.DONE
            res.value = result.data?.sortedBy { it.date } ?: emptyList()
        } else {
            Log.d(TAG, "result is not success")
            res.value = emptyList()
            _storeDataStatus.value = StoreDataStatus.ERROR
            if (result is Error)
                Log.d(TAG, "getMessagesLiveData: Error Occurred: $result")
        }
        return res
    }

    private fun getChatsLiveData(result: Result<List<Chat>?>?): LiveData<List<Chat>> {
        val res = MutableLiveData<List<Chat>>()
        if (result is Success) {
            Log.d(TAG, "result is success")
            _storeDataStatus.value = StoreDataStatus.DONE
            res.value = result.data!!
        } else {
            Log.d(TAG, "result is not success")
            res.value = emptyList()
            _storeDataStatus.value = StoreDataStatus.ERROR
            if (result is Error)
                Log.d(TAG, "getChatLiveData: Error Occurred: $result")
        }
        return res
    }



    fun observeRemoteChat(chatId: String){
        viewModelScope.launch {
            messageRepository.observeMessagesOnRemoteByChatId(chatId){ messages ->
                if (_chatMessages.value != null){
                    refreshMessages(messages, _chatMessages.value!!)
                }else{
                    refreshMessages(messages, emptyList())
                }
            }
        }
    }



    fun observeLocalMessages(chatId: String) {
        _chatMessages = Transformations.switchMap(messageRepository.observeMessagesOnLocalByChatId(chatId)) {
            getMessagesLiveData(it)
        } as MutableLiveData<List<Message>>
    }


    fun observeLocalChats() {
        _chats = Transformations.switchMap(chatRepository.observeChats()) {
            getChatsLiveData(it)
        } as MutableLiveData<List<Chat>>
    }


    fun observeLocalMessages(){
        _allMessages = Transformations.switchMap(messageRepository.observeMessage()) {
            getMessagesLiveData(it)
        } as MutableLiveData<List<Message>>
    }


    fun getLastMessage(chatId: String){
        viewModelScope.launch {
            val res = messageRepository.getLastMessage(chatId)
            if (res is Success){
                val lastMessage = res.data
                Log.d(TAG,"lastMessage: ${lastMessage?.text}")
            }
        }
    }

    fun removeAllMessages(){
        viewModelScope.launch {
            messageRepository.deleteAllMessages()
        }
    }


    fun deleteMessage(message: Message) {
        viewModelScope.launch {
            messageRepository.deleteMessage(message)
        }
    }


    private fun refreshMessages(remoteMessages: List<Message>, localMessages: List<Message>) {
        viewModelScope.launch {
                Log.d(TAG,"remote Messages: ${remoteMessages.size}")
                Log.d(TAG,"local Messages: ${localMessages.size}")
                val news = remoteMessages.findDiffElements(localMessages){it.messageId}
                Log.d(TAG,"new Messages: ${news.size}")

            if(news.isNotEmpty()){
                _newMessages.value = news
                messageRepository.insertMultipleMessages(news)
                val lastMessage = news.last()
                updateChat(lastMessage)

            }

            viewModelScope.launch {
                if(localMessages.size > remoteMessages.size) {
                    val removed = localMessages.findDiffElements(remoteMessages){it.messageId}
                    messageRepository.deleteMultipleMessages(removed)
                    val lastM = removed.last()
                    updateChat(lastM)
                    Log.d(TAG,"removed Messages: ${removed.size}")
                }
            }
        }
    }
    

    fun getChatMessages(chatId: String){
        viewModelScope.launch {
            val res = messageRepository.getMessagesByChatId(chatId)
            if (res is Success){

            }
        }
    }




    fun sendingComplete(){
        _isSending.value = false
    }



    fun sendMessage(text: String,chatId: String,recipient: User,type: MessageType,imgList:List<Uri>){
        viewModelScope.launch {
            val message: Message = when(type){
                MessageType.TEXT-> {
                    Message(getMessageId(userId!!,chatId), text, Date(), userId, recipient.userId, emptyList() , chatId,type.name)
                }
                MessageType.IMAGE ->{
                    val resImg = async { messageRepository.insertImages(imgList) }
                    val imagesPaths = resImg.await()
                    Message(getMessageId(userId!!,chatId), text, Date(), userId, recipient.userId, imagesPaths , chatId,type.name)
                }
                MessageType.TEXT_IMAGE->{
                    val resImg = async { messageRepository.insertImages(imgList) }
                    val imagesPaths = resImg.await()
                    Message(getMessageId(userId!!,chatId), text, Date(), userId, recipient.userId, imagesPaths , chatId,type.name)
                }
            }
            messageRepository.insertMessage(message)
            updateChat(message)
        }
    }


    private suspend fun updateChat(message: Message){
        val res = chatRepository.getChats(message.senderId)
        if (res is Success){
            val data = res.data
            val chat = data?.filter { it.chatId == message.chatId }?.toMutableList()?.get(0)
            if(chat != null){
                chat.lastUpdate = message.date
                chat.lastMessage = message.text
                chatRepository.updateChat(chat)
            }
        }
    }




//    fun addMessage(message: Message,image: Uri? = null){
//
//        scopeIO.launch {
//
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build()
//
//            val dateToLong = DateTypeConverter().to(message.date)
//
//            val data = Data.Builder()
//            data.putString("message_id",message.messageId)
//            data.putString("text",message.text)
//            data.putLong("date", dateToLong!!)
//            data.putString("sender_id",message.senderId)
//            data.putString("recipient_id",message.recipientId)
//            data.putString("image_id",message.image)
//            data.putString("type",message.type)
//            data.putString("chat_id",chatId)
//            data.putString("image", image.toString())
//
//            uploadImageWorker
//                .setInputData(data.build())
//                .setConstraints(constraints)
//
//            sendMessageWorker
//                .setInputData(data.build())
//                .setConstraints(constraints)
//
//            val uploadImageBuilder = uploadImageWorker.build()
//            val sendMessageBuilder = sendMessageWorker.build()
//
//            if (image != null){ // image it will uploader then message it will be sent
//                worker.beginWith(uploadImageBuilder)
//                    .then(sendMessageBuilder)
//                    .enqueue()
//
//                withContext(Dispatchers.Main){
//                    _isSending.value = true
//                }
//            }else{
//                worker.beginWith(sendMessageBuilder).enqueue()
//            }
//
//            withContext(Dispatchers.Main){
//                _uploadImageWorkerId.value = uploadImageBuilder.id
//                _sendMessageWorkerId.value = sendMessageBuilder.id
//            }
//
//        }
//
//    }

//    fun removeMessage(message: Message){
//        scopeIO.launch {
//            val constraints = Constraints.Builder()
//                .setRequiredNetworkType(NetworkType.CONNECTED)
//                .build()
//
//            val data = Data.Builder()
//            data.putString("chat_id",chatId)
//            data.putString("message_id",message.messageId.toString())
//
//            removeMessageWorker
//                .setInputData(data.build())
//                .setConstraints(constraints)
//
//            val builder = removeMessageWorker.build()
//
//            worker.beginWith(builder).enqueue()
//
//            withContext(Dispatchers.Main){
//                _removeMessageWorkerId.value = builder.id
//            }
//        }
//    }


//    private fun createNewChatId(chatId:(String) -> Unit) {
//        val newChatId = genUUID()
//        scopeIO.launch {
//            _root.collection("users")
//                .document(_auth.currentUser!!.uid)
//                .collection("chats")
//                .document(userId!!).get().addOnSuccessListener {
//                    if (it.exists()){
//                        val chat = it.toObject(Chat::class.java)
//                        Log.i(TAG,"old chat id: ${chat!!.chatId}")
//                        chatId(chat.chatId) // get chat id
//                        getMessages(chat.chatId)
//                    }else{
//                        chatId(newChatId) // create new chat id
//                        getMessages(newChatId)
//                        scopeIO.launch {
//                            val senderId = _auth.currentUser!!.uid
//                            val chat = Chat(newChatId,senderId,userId,Calendar.getInstance().time)
//
//                            _root.collection("users")
//                                .document(senderId)
//                                .collection("chats")
//                                .document(userId)
//                                .set(chat)
//
//                            _root.collection("users")
//                                .document(userId)
//                                .collection("chats")
//                                .document(senderId)
//                                .set(chat)
//                        }
//                    }
//                }
//        }
//    }
//


//
//    private fun getMessages(chatId: String?){
//        scopeIO.launch {
//            if (chatId != null){
//                repositoryOnline.getMessages(chatsPath.document(chatId).collection("messages")).orderBy("time")
//                    .addSnapshotListener { value, error ->
//                        if (error == null){
//                            if (value != null){
//                                val messages = value.toObjects(Message::class.java) // here you will not receive the message each time when the data is updated
//                                _messages.value = messages
//
//                            }
//                        }
//                    }
//            }else{
//                Log.i(TAG,"chat id is not found!")
//            }
//        }
//    }
//






    override fun onCleared() {
        super.onCleared()

    }
}