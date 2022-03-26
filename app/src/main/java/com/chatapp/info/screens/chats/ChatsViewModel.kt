package com.chatapp.info.screens.chats

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatapp.info.ChatApplication
import com.chatapp.info.data.ChatDetails

import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.screens.chat.ChatViewModel
import com.chatapp.info.utils.ChatAppSessionManager
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.StoreDataStatus
import com.chatapp.info.utils.findDiffElements
import kotlinx.coroutines.launch

class ChatsViewModel(application: Application) : AndroidViewModel(application) {

    companion object{
        const val TAG = "ChatsViewModel"
    }
    private val chatApplication by lazy { ChatApplication(application) }
    private val messageRepository by lazy { chatApplication.messageRepository }
    private val userRepository by lazy { chatApplication.userRepository }

    private val appSession by lazy { ChatAppSessionManager(application) }
    private val userId = appSession.getUserIdFromSession()

    private var _allMessages = MutableLiveData<List<Message>>()
    val allMessages: LiveData<List<Message>> get() = _allMessages


    private var _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> get() = _currentUser



    init {
//        observeLocalMessages()
        observeLocalUser()
    }




    fun observeLocalUser(){
        _currentUser = Transformations.switchMap(userRepository.observeLocalUser(userId!!)) {
            getUserLiveData(it!!)
        } as MutableLiveData<User>
    }


    private fun getUserLiveData(result: Result<User>): LiveData<User?> {
        val res = MutableLiveData<User?>()
        if (result is Result.Success) {
            Log.d(ChatViewModel.TAG, "result is success")
            res.value = result.data
        } else {
            Log.d(ChatViewModel.TAG, "result is not success")
            if (result is Result.Error)
                Log.d(ChatViewModel.TAG, "getMessagesLiveData: Error Occurred: $result")
        }
        return res
    }




//    private fun observeLocalMessages() {
//        _allMessages = Transformations.switchMap(messageRepository.observeMessage()) {
//            getMessagesLiveData(it)
//        } as MutableLiveData<List<Message>>
//    }
//
//    private fun getMessagesLiveData(result: Result<List<Message>?>?): LiveData<List<Message>> {
//        val res = MutableLiveData<List<Message>>()
//        if (result is Result.Success) {
//            Log.d(TAG, "result is success")
//            res.value = result.data?.sortedBy { it.date } ?: emptyList()
//        } else {
//            Log.d(TAG, "result is not success")
//            res.value = emptyList()
//            if (result is Result.Error)
//                Log.d(TAG, "getMessagesLiveData: Error Occurred: $result")
//        }
//        return res
//    }




// TODO: observe remote messages and update messages


    fun observeRemoteChat(chatId: String){
        viewModelScope.launch {
            messageRepository.observeMessagesOnRemoteByChatId(chatId){ messages ->
                if (_allMessages.value != null){
                    refreshMessages(messages, _allMessages.value!!)
                }else{
                    refreshMessages(messages, emptyList())
                }
            }
        }
    }



    private fun refreshMessages(remoteMessages: List<Message>, localMessages: List<Message>) {
        viewModelScope.launch {
            Log.d(ChatViewModel.TAG,"remote Messages: ${remoteMessages.size}")
            Log.d(ChatViewModel.TAG,"local Messages: ${localMessages.size}")
            val news = remoteMessages.findDiffElements(localMessages){it.messageId}
            _allMessages.value = news
            Log.d(ChatViewModel.TAG,"new Messages: ${news.size}")
            messageRepository.insertMultipleMessages(news)

            viewModelScope.launch {
                if(localMessages.size > remoteMessages.size){
                    val removed = localMessages.findDiffElements(remoteMessages){it.messageId}
                    messageRepository.deleteMultipleMessages(removed)
                    Log.d(ChatViewModel.TAG,"removed Messages: ${removed.size}")
                }
            }
        }
    }




    override fun onCleared() {
        super.onCleared()
    }


}