package com.chatapp.info.di

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.chatapp.info.data.Chat
import com.chatapp.info.local.ChatAppDataBase
import com.chatapp.info.local.api.ChatApi
import com.chatapp.info.local.api.MessageApi
import com.chatapp.info.repository.chat.ChatRepository
import com.chatapp.info.repository.chat.LocalChatRepository
import com.chatapp.info.repository.chat.RemoteChatRepository
import com.chatapp.info.repository.message.LocalMessageRepository
import com.chatapp.info.repository.message.MessageRepository
import com.chatapp.info.repository.message.RemoteMessageRepository
import com.chatapp.info.repository.user.LocalUserRepository
import com.chatapp.info.repository.user.RemoteUserRepository
import com.chatapp.info.repository.user.UserRepository
import com.chatapp.info.screens.account.AccountViewModel
import com.chatapp.info.screens.chat.ChatViewModel
import com.chatapp.info.screens.chats.ChatsViewModel
import com.chatapp.info.screens.login.LoginViewModel
import com.chatapp.info.screens.registration.RegistrationViewModel
import com.chatapp.info.screens.users.UsersViewModel
import com.chatapp.info.utils.ChatAppSessionManager
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.findDiffElements
import com.chatapp.info.utils.getChatId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module
import kotlin.coroutines.suspendCoroutine
import com.chatapp.info.local.api.UserApi as UserApi

val viewModelModule = module {

    viewModel {
        ChatViewModel(
            chatRepository = get() ,
            userRepository = get(),
            messageRepository = get()
        )
    }

    viewModel {
        LoginViewModel(userRepository = get())
    }

    viewModel {
        RegistrationViewModel(userRepository = get())
    }

    viewModel {
        AccountViewModel(userRepository = get())
    }

    viewModel {
        UsersViewModel(
            userRepository = get(),
            chatRepository = get(),
            chats = get()
        )
    }

    viewModel {
        ChatsViewModel(
            chatRepository = get(),
            userRepository = get()
        )
    }


}

val messageRepoModule = module {
    single { RemoteMessageRepository() }
    single { LocalMessageRepository(messageApi = get()) }
    single { MessageRepository(localMessageRepository = get(), remoteMessageRepository = get()) }
}

val userRepoModule = module {
    single { RemoteUserRepository() }
    single { LocalUserRepository(userApi = get()) }
    single { ChatAppSessionManager(androidContext()) }
    single { UserRepository(localUserRepository = get(), remoteUserRepository = get(), sessionManager = get()) }
}

val chatRepoModule = module {
    single { RemoteChatRepository() }
    single { LocalChatRepository(chatApi = get()) }
    single { ChatRepository(chatLocalRepository = get(), chatRemoteRepository = get()) }
}




val remoteDataBaseModule = module {

    fun getChatsLiveData(result: Result<List<Chat>>): LiveData<List<Chat>?> {
        val res = MutableLiveData<List<Chat>?>()
        if (result is Result.Success) {
            Log.d(ChatViewModel.TAG, "result is success")
            res.value = result.data
        } else {
            Log.d(ChatViewModel.TAG, "result is not success")
            if (result is Result.Error)
                res.value = null
            Log.d(ChatViewModel.TAG, "getMessagesLiveData: Error Occurred: $result")
        }
        return res
    }


    fun observeLocalChats(chatRepository: ChatRepository): LiveData<List<Chat>?> {
         return Transformations.switchMap(chatRepository.observeChats()) {
            getChatsLiveData(it!!)
        }as MutableLiveData<List<Chat>>
    }


    single { observeLocalChats(chatRepository = get()) }


}



val chatDataBaseModule = module {

    fun createDataBase(application: Application): ChatAppDataBase {
        return Room.databaseBuilder(
            application, ChatAppDataBase::class.java, "ChatDataBase31")
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    fun userApi(dataBase: ChatAppDataBase): UserApi{
        return dataBase.localUserApi()
    }

    fun chatApi(dataBase: ChatAppDataBase): ChatApi{
        return dataBase.localChatApi()
    }

    fun messageApi(dataBase: ChatAppDataBase): MessageApi{
        return dataBase.localMessageApi()
    }

    single { createDataBase(androidApplication()) }
    single { userApi(dataBase = get()) }
    single { chatApi(dataBase = get()) }
    single { messageApi(dataBase = get()) }
}




