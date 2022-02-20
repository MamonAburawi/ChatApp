//package com.chatapp.info.workmanager
//
//import android.content.Context
//import android.util.Log
//import androidx.work.CoroutineWorker
//import androidx.work.Data
//import androidx.work.WorkerParameters
//import androidx.work.workDataOf
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
//@DelicateCoroutinesApi
//class IsUserExistWork(context : Context, paramsWorker: WorkerParameters) : CoroutineWorker(context,paramsWorker) {
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
//    private val data = Data.Builder()
//    private val email = inputData.getString("email")!!
//
//    override suspend fun doWork(): Result {
//
//        try {
//
//            GlobalScope.launch(Dispatchers.IO) {
//                repository.getUsers(_usersPath).get().addOnSuccessListener { documents ->
//                    val users = documents.toObjects(User::class.java)
//                    users.forEach { user ->
//                        if (user.email == _email){
//                            _email = email
//                        }
//                    }
//                    if (_email == email ){
//                        data.putBoolean("isUserExist",true) // user is exist
//                        Log.i(RegistrationViewModel.TAG,"user is exist!")
//                    }else{
//                        data.putBoolean("isUserExist",false) // user is not exist
//                        Log.i(RegistrationViewModel.TAG,"user is not exist!")
//                    }
//                    val builder = data.build()
//
//
//
//
//                }
//
//            }
//
//
//
//        }catch (ex: java.lang.Exception){
//            Log.i(RegistrationViewModel.TAG,ex.message.toString())
//
//        }
//
//
//        return Result.success()
//    }
//}