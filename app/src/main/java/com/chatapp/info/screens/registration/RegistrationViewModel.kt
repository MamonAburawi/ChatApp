package com.chatapp.info.screens.registration


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chatapp.info.ChatApplication
import com.chatapp.info.R
import com.chatapp.info.data.User
import com.chatapp.info.repository.user.RemoteUserRepository
import com.chatapp.info.repository.user.UserRepository
import com.chatapp.info.utils.StoreDataStatus
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*


class RegistrationViewModel(private val userRepository: UserRepository): ViewModel() {

    companion object{
        const val TAG = "Registration"
    }

//    private val app = application


//    private val shopApp = ChatApplication(application.applicationContext)
//    private val userRepository by lazy{ shopApp.userRepository }

    private val _authRemoteDataSource by lazy { RemoteUserRepository() }

    //    private val repository = UserRepositoryOnline()
    private val scopeIO = CoroutineScope(Dispatchers.IO + Job())

    private var e = ""

    /** live data **/
    private val _inProgress = MutableLiveData<StoreDataStatus?>()
    val inProgress: LiveData<StoreDataStatus?> = _inProgress

    /** live data **/
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage



    /** live data **/
    private val _isRegistered = MutableLiveData<User?>()
    val isRegistered: LiveData<User?> = _isRegistered



    // firebase authentication
    private val _auth = FirebaseAuth.getInstance()



    fun initRegister(){
        _errorMessage.value = null
        _isRegistered.value = null
        _inProgress.value = null
    }

    fun setRegistrationError(error: String){
        _errorMessage.value = error
        _inProgress.value = StoreDataStatus.ERROR
    }

    fun registration(user: User){
        initRegister()
        scopeIO.launch {
            withContext(Dispatchers.Main){
                _inProgress.value = StoreDataStatus.LOADING
            }
            _authRemoteDataSource.checkUserIsExist(user.email,
                isExist = { isExist ->
                    if (!isExist){ // user is not exist
                        createUserAccount(user)
                        Log.i(TAG,"user is not exist")
                    }else{// user is exist
                        Log.i(TAG,"user is exist")
                        setRegistrationError("account is already used!")
                    }
                },
                onError = {error ->
                    Log.i(TAG,error)
                    setRegistrationError(error)
                })
        }
    }


    // TODO: change the user id let it start by userId-xxxxxxxxx

    private fun createUserAccount(user: User){
        scopeIO.launch {
            _auth.createUserWithEmailAndPassword(user.email, user.password).addOnCompleteListener { it->
                if (it.isComplete){
                    Log.i(TAG,"create new account is done!")
                    sendEmailVerify().addOnCompleteListener { task ->
                        if(task.isComplete){
                            scopeIO.launch {
                                Log.i(TAG,"send email verify has been done!")
                                user.userId = FirebaseAuth.getInstance().currentUser!!.uid
                                userRepository.signUp(user)
//                                    _authRemoteDataSource.addUser(user)
                                withContext(Dispatchers.Main){
                                    _isRegistered.value = user
                                    _inProgress.value = StoreDataStatus.DONE
                                }
                            }
                        }else{
                            val error = it.exception!!.message.toString()
                            _inProgress.value = StoreDataStatus.ERROR
                            Log.i(TAG,error)
                        }
                    }
                }else{
                    _inProgress.value = StoreDataStatus.ERROR
                    val error = it.exception!!.message.toString()
                    Log.i(TAG,error)
                }
            }

        }
    }


    private fun sendEmailVerify() = _auth.currentUser!!.sendEmailVerification()


    override fun onCleared() {
        super.onCleared()

        scopeIO.cancel()
    }
}