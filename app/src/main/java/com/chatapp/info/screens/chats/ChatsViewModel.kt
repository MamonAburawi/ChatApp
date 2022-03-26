package com.chatapp.info.screens.chats

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatapp.info.ChatApplication

import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.screens.chat.ChatViewModel
import com.chatapp.info.utils.ChatAppSessionManager
import com.chatapp.info.utils.Result

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


    private val _chats = MutableLiveData<List<User.Chat>?>()
    val chats: LiveData<List<User.Chat>?> = _chats


    init {
        observeLocalUser()
    }



    private fun observeLocalUser(){
        _currentUser = Transformations.switchMap(userRepository.observeLocalUser(userId!!)) {
            getUserLiveData(it!!)
        } as MutableLiveData<User>
    }

    fun initChats(chats: List<User.Chat>?){
        _chats.value = chats
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




    override fun onCleared() {
        super.onCleared()
    }


}