package com.chatapp.info.screens.users

import android.util.Log
import androidx.lifecycle.*
import com.chatapp.info.data.Chat
import com.chatapp.info.data.User
import com.chatapp.info.repository.chat.ChatRepository
import com.chatapp.info.repository.user.UserRepository
import com.chatapp.info.screens.chat.ChatViewModel
import com.chatapp.info.utils.*
import kotlinx.coroutines.*
import java.util.*

class UsersViewModel(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val chats: LiveData<List<Chat>>): ViewModel() {

    companion object{
        const val TAG = "UsersViewModel"
    }

    private val appManager = userRepository.sessionManager
    private val userId = appManager.getUserIdFromSession()

    private var _currentUser = MutableLiveData<User>()
    val currentUser: LiveData<User> = _currentUser

    private val scopeIO = CoroutineScope(Dispatchers.IO + Job())
    private val scopeMain = CoroutineScope(Dispatchers.Main + Job())

    private var _users = MutableLiveData<List<User>?>()
    val users: LiveData<List<User>?> get() = _users

    private val _navigateToChat = MutableLiveData<String?>()
    val navigateToChat: LiveData<String?> = _navigateToChat

    private var _localChats = MutableLiveData<List<Chat>>()
    val localChats: LiveData<List<Chat>> get() = _localChats



    // todo get live data of users from the local .
    // todo observe the data of users and update local users data.


    init {
//        _signOut.value = false
//        getCurrentUser()
        hardRefreshUsesData()
        observeLocalUsers()
//        refreshChats()
        observeRemoteChats()
//        observeLocalChats()
//        _localChats = chats as MutableLiveData<List<Chat>>
    }

    fun initData(){
        _localChats = chats as MutableLiveData<List<Chat>>
    }


//    private fun observeLocalChats(){
//        _localChats = Transformations.switchMap(chatRepository.observeChats()) {
//            getChatsLiveData(it!!)
//        } as MutableLiveData<List<Chat>>
//    }
//
//    private fun getChatsLiveData(result: Result<List<Chat>>): LiveData<List<Chat>?> {
//        val res = MutableLiveData<List<Chat>?>()
//        if (result is Result.Success) {
//            Log.d(ChatViewModel.TAG, "result is success")
//            res.value = result.data
//        } else {
//            Log.d(ChatViewModel.TAG, "result is not success")
//            if (result is Result.Error)
//                res.value = null
//                Log.d(ChatViewModel.TAG, "getMessagesLiveData: Error Occurred: $result")
//        }
//        return res
//    }
//





    fun navigateToChat(recipient: User){
        viewModelScope.launch {
            val chats = _localChats.value
            if (chats != null){
                val chat = chats.find { it.recipientId == userId }
                if (chat != null) {
                    Log.d(TAG,"current chatId = ${chat.chatId}")
                    _navigateToChat.value = chat.chatId
                }else{
                    val newChatId = getChatId(userId!!,recipient.userId)
                    _navigateToChat.value = newChatId
                    val newChat = Chat(newChatId,userId,recipient.userId,"",recipient.name,"","", Date())
                    chatRepository.insertChat(newChat)
                }
            }
        }
    }




    private fun observeRemoteChats(){
        viewModelScope.launch {
            userRepository.observeUserChatsIds(userId!!){ chats ->
                viewModelScope.launch {
                    val res =  chatRepository.getChats(userId)

                    if(res is Result.Success){
                        val c = res.data?.toMutableSet()
                        val news = chats.findDiffElements(c!!){it.chatId}

                        if (news.isNotEmpty()){
                            Log.d(TAG,"this new chats inserted in local ${news.size} ${news[0].chatId}")
                            chatRepository.insertMultipleChats(news)
                        }
                    }
                }
            }
        }
    }




    private fun updateUser(user: User){
        viewModelScope.launch {
            val res = async { userRepository.updateUser(user) }
            res.await()

        }
    }

    fun navigateToChatDone(){
        _navigateToChat.value = null
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



    fun observeLocalUsers(){
        _users = Transformations.switchMap(userRepository.observeUsers()) {
            getUsersLiveData(it!!)
        } as MutableLiveData<List<User>?>
    }


    private fun getUsersLiveData(result: Result<List<User>?>): LiveData<List<User>?> {
        val res = MutableLiveData<List<User>?>()
        if (result is Result.Success) {
            Log.d(ChatViewModel.TAG, "result is success")
            val allUsers = result.data

            // remove current user
            if (allUsers != null){
                val currentUser = allUsers.find { it.userId == userId }
                val list = allUsers.toMutableList()
                list.remove(currentUser)

                res.value = list.toList()
            }

        } else {
            Log.d(ChatViewModel.TAG, "result is not success")
            if (result is Result.Error)
                Log.d(ChatViewModel.TAG, "getMessagesLiveData: Error Occurred: $result")
        }
        return res
    }



    fun getLocalUsers() {
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



//    fun signOut(){
//        scopeIO.launch {
//            userRepository.signOut()
////            _auth.signOut()
//            withContext(Dispatchers.Main){
//                _signOut.value = true
//            }
//        }
//    }


    override fun onCleared() {
        super.onCleared()

        scopeIO.cancel()
        scopeMain.cancel()
    }

}