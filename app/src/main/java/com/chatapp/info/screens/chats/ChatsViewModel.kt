package com.chatapp.info.screens.chats

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatapp.info.ChatApplication
import com.chatapp.info.data.Chat

import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.screens.chat.ChatViewModel
import com.chatapp.info.utils.ChatAppSessionManager
import com.chatapp.info.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatsViewModel(application: Application) : AndroidViewModel(application) {

    companion object{
        const val TAG = "ChatsViewModel"
    }
    private val chatApplication by lazy { ChatApplication(application) }
    private val messageRepository by lazy { chatApplication.messageRepository }
    private val userRepository by lazy { chatApplication.userRepository }
    private val chatRepository by lazy { chatApplication.chatRepository }

    private val appSession by lazy { ChatAppSessionManager(application) }
    private val userId = appSession.getUserIdFromSession()

    private var _allMessages = MutableLiveData<List<Message>>()
    val allMessages: LiveData<List<Message>> get() = _allMessages

    private var _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> get() = _currentUser

    private var _localChats = MutableLiveData<List<Chat>>()
    val localChats: LiveData<List<Chat>> get() = _localChats

    private var _navigateToChat = MutableLiveData<User?>()
    val navigateToChat: LiveData<User?> get() = _navigateToChat



//    private val _chats = MutableLiveData<List<User.Chat>?>()
//    val chats: LiveData<List<User.Chat>?> = _chats
//

    init {
        observeLocalUser()
    }


    fun navigateToChat(userId: String){
        viewModelScope.launch {
            val user = userRepository.getUser(userId)
            withContext(Dispatchers.Main){
                _navigateToChat.value = user
            }
        }
    }

    fun navigateToChatDone(){
        _navigateToChat.value = null
    }

    private fun observeLocalUser(){
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



    private fun observeLocalChats(){
        _localChats = Transformations.switchMap(chatRepository.observeChats()) {
            getChatsLiveData(it!!)
        } as MutableLiveData<List<Chat>>
    }

    private fun getChatsLiveData(result: Result<List<Chat>>): LiveData<List<Chat>?> {
        val res = MutableLiveData<List<Chat>?>()
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





    override fun onCleared() {
        super.onCleared()
    }


}