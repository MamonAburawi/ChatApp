package com.chatapp.info.screens.users

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.chatapp.info.ChatApplication
import com.chatapp.info.data.User
import com.chatapp.info.utils.ChatAppSessionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList

class UsersViewModel(application: Application) : AndroidViewModel(application) {


    private val sessionManager by lazy { ChatAppSessionManager(application) }
    private val chatApplication by lazy { ChatApplication(application) }

    private val userRepository by lazy { chatApplication.userRepository }

    private val userId = sessionManager.getUserIdFromSession()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser


    private val scopeIO = CoroutineScope(Dispatchers.IO + Job())
    private val scopeMain = CoroutineScope(Dispatchers.Main + Job())

    private val _users = MutableLiveData<List<User>?>()
    val users: LiveData<List<User>?> = _users

    private val _signOut = MutableLiveData<Boolean?>()
    val signOut: LiveData<Boolean?> = _signOut

    // firebase fire store
    private val _root = FirebaseFirestore.getInstance()
    private val _usersPath = _root.collection("users")

    // firebase authentication
    private val _auth = FirebaseAuth.getInstance()

    private var user: User? = null


    init {
        _signOut.value = false
        getUser()
        getAllUser()
    }





     fun getAllUser(){
        viewModelScope.launch {
            val users = userRepository.getAllUsers()
            withContext(Dispatchers.Main){
                users as ArrayList<User>
                users.remove(_currentUser.value)
                _users.value = users
            }
        }
    }

    
    fun hardRefreshData(){
        viewModelScope.launch {
            userRepository.hardRefreshData()
        }
    }

    fun getUser() {
        viewModelScope.launch {
            val user = userRepository.getUser(userId!!)
            withContext(Dispatchers.Main){
                _currentUser.value = user
            }
        }
    }


    fun filterBySearch(query: String) {
        if (query.isEmpty()){
            getAllUser()
        }else{
            val users = _users.value
            _users.value = users?.filter { it.name.lowercase().contains(query.lowercase()) }
        }

    }



    fun signOut(){
        scopeIO.launch {
            _auth.signOut()
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