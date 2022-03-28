package com.chatapp.info.screens.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chatapp.info.ChatApplication
import com.chatapp.info.utils.ChatAppSessionManager
import kotlinx.coroutines.launch

class AccountViewModel(application: Application) : AndroidViewModel(application) {


    private val sessionManager by lazy { ChatAppSessionManager(application) }
    private val chatApplication by lazy { ChatApplication(application) }
    private val userRepository by lazy { chatApplication.userRepository }
    private val userId = sessionManager.getUserIdFromSession()

    private val _signOut = MutableLiveData<Boolean?>()
    val signOut: LiveData<Boolean?> = _signOut






    fun signOut(){
        viewModelScope.launch {
            userRepository.signOut()
            _signOut.value = true
        }
    }

}