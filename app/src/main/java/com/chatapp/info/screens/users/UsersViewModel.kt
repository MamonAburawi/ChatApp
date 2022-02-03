package com.chatapp.info.screens.users

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.chatapp.info.data.User
import com.chatapp.info.repository.server.UserRepositoryOnline
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.util.*

class UsersViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepositoryOnline: UserRepositoryOnline by lazy {
        UserRepositoryOnline()
    }

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
        getUserInfo {
            user = it
        }

    }




    fun filter(charSequence: String?,onComplete:(List<User>)-> Unit){
        scopeIO.launch {
            userRepositoryOnline.getUsers(_usersPath).get().addOnSuccessListener { it ->
                if(it != null){
                        val users = it.toObjects(User::class.java)
                        onComplete(users.filter { it.name.lowercase(Locale.getDefault()).contains(charSequence!!.trim())})
                    }
            }
        }
    }


    private fun getUserInfo(onComplete: (User) -> Unit){
        scopeIO.launch {
            userRepositoryOnline.getUser(_usersPath.document(_auth.currentUser!!.uid)).get().addOnSuccessListener {
                if(it != null){
                    val user = it.toObject(User::class.java)
                    if (user != null) {
                        onComplete(user)
                    }
                }
            }
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

    fun currentUser() = user

    override fun onCleared() {
        super.onCleared()

        scopeIO.cancel()
        scopeMain.cancel()
    }
}