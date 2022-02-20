//package com.chatapp.info.workmanager
//
//import android.content.Context
//import android.util.Log
//import androidx.work.CoroutineWorker
//import androidx.work.WorkerParameters
//import com.chatapp.info.MessageState
//import com.chatapp.info.data.User
//import com.chatapp.info.repository.server.UserRepositoryOnline
//import com.chatapp.info.screens.chat.ChatViewModel
//import com.chatapp.info.screens.registration.RegistrationViewModel
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.DelicateCoroutinesApi
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//
//
//class RegistrationWork(context : Context, paramsWorker: WorkerParameters) : CoroutineWorker(context,paramsWorker) {
//
//    // firebase fire store
//    private val _root = FirebaseFirestore.getInstance()
//    private val _usersPath = _root.collection("users")
//
//    // firebase authentication
//    private val _auth = FirebaseAuth.getInstance()
//    private val repository = UserRepositoryOnline()
//    private var _email = ""
//
//
//
//    override suspend fun doWork(): Result {
//
//        try {
//
//        }catch (ex: Exception){
//
//            return Result.failure()
//        }
//
//        return Result.success()
//    }
//
//
//    private fun registration(){
//
//        try {
//            isUserExist(user.email){
//                if (it){ // user is exist
//                    _inProgress.value = false
//                    _error.value = "User is Exist!"
//                }else{ // user is not exist
//                    scopeIO.launch {
//                        createUserAccount(user)
//                    }
//                }
//            }
//
//
//        }catch (ex: java.lang.Exception){
//            Log.i(RegistrationViewModel.TAG,ex.message.toString())
//
//        }
//    }
//
//
//
//
//}