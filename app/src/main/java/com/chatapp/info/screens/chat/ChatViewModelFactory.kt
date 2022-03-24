package com.chatapp.info.screens.chat


import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.chatapp.info.data.User

class ChatViewModelFactory(private val application: Application,
                           private val recipient: User,
                           private val chatId: String): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(application,recipient, chatId  ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}