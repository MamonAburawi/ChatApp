package com.chatapp.info.screens.account


import androidx.lifecycle.*
import com.chatapp.info.repository.user.UserRepository
import kotlinx.coroutines.launch

class AccountViewModel(
    private val userRepository: UserRepository,
) :ViewModel() {

    private val userId = userRepository.sessionManager.getUserIdFromSession()

//    private val sessionManager by lazy { ChatAppSessionManager(application) }
//    private val chatApplication by lazy { ChatApplication(application) }
//    private val userRepository by lazy { chatApplication.userRepository }


    private val _signOut = MutableLiveData<Boolean?>()
    val signOut: LiveData<Boolean?> = _signOut






    fun signOut(){
        viewModelScope.launch {
            userRepository.signOut()
            _signOut.value = true
        }
    }

}