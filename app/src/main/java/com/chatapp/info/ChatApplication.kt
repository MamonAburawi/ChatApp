package com.chatapp.info

import android.app.Application
import android.content.Context
import com.chatapp.info.repository.message.MessageRepoInterface
import com.chatapp.info.repository.user.UserRepoInterface

class ChatApplication(private val context: Context): Application() {

    val userRepository: UserRepoInterface
        get() = ServiceLocator.provideUserRepository(context)

    val messageRepository: MessageRepoInterface
        get() = ServiceLocator.provideMessageRepository(context)


    override fun onCreate() {
        super.onCreate()
    }
}