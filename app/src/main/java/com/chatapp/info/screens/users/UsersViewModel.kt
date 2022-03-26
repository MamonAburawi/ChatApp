package com.chatapp.info.screens.users

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.chatapp.info.ChatApplication
import com.chatapp.info.data.User
import com.chatapp.info.screens.chat.ChatViewModel
import com.chatapp.info.utils.ChatAppSessionManager
import com.chatapp.info.utils.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class UsersViewModel(application: Application) : AndroidViewModel(application) {


    private val sessionManager by lazy { ChatAppSessionManager(application) }
    private val chatApplication by lazy { ChatApplication(application) }

    private val userRepository by lazy { chatApplication.userRepository }

    private val userId = sessionManager.getUserIdFromSession()

    private var _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser


    private val scopeIO = CoroutineScope(Dispatchers.IO + Job())
    private val scopeMain = CoroutineScope(Dispatchers.Main + Job())

    private val _users = MutableLiveData<List<User>?>()
    val users: LiveData<List<User>?> = _users

    private val _signOut = MutableLiveData<Boolean?>()
    val signOut: LiveData<Boolean?> = _signOut




    // todo get live data of users from the local .
    // todo observe the data of users and update local users data.


    init {
        _signOut.value = false
//        getCurrentUser()
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



    fun getLocalUsers(){
        viewModelScope.launch {
            val users = userRepository.getAllUsers()
            if (users != null){
                val currentUser = users.filter { it.userId == userId }[0]
                val list = users.toMutableList()
                list.remove(currentUser)
                withContext(Dispatchers.Main){
                    _users.value = list
                    _currentUser.value = currentUser
                }
            }
        }
    }


    fun observeRemoteUser(){
        viewModelScope.launch {
            userRepository.observeAndRefreshUser(userId!!)
        }
    }


    fun hardRefreshUsesData(){
        viewModelScope.launch {
            userRepository.hardRefreshUsersData()
        }
    }


//    fun getCurrentUser() {
//        viewModelScope.launch {
//            val user = getUser(userId!!)
//            withContext(Dispatchers.Main){
//                _currentUser.value = user!!
//            }
//        }
//    }
//


    suspend fun getUser(userId : String): User? {
        return userRepository.getUser(userId)
    }


    fun filterBySearch(query: String) {
        if (query.isEmpty()){
            getLocalUsers()
        }else{
            val users = _users.value
            _users.value = users?.filter { it.name.lowercase().contains(query.lowercase()) }
        }

    }



    fun signOut(){
        scopeIO.launch {
            userRepository.signOut()
//            _auth.signOut()
            withContext(Dispatchers.Main){
                _signOut.value = true
            }
        }
    }


    override fun onCleared() {
        super.onCleared()

        scopeIO.cancel()
        scopeMain.cancel()
    }

}