package com.chatapp.info

import android.app.Application
import android.content.Context
import com.chatapp.info.repository.chat.ChatRepository
import com.chatapp.info.repository.message.MessageRepository
import com.chatapp.info.repository.user.UserRepository

class ChatApplication(private val context: Context): Application() {

    val userRepository: UserRepository
        get() = ServiceLocator.provideUserRepository(context)

    val messageRepository: MessageRepository
        get() = ServiceLocator.provideMessageRepository(context)

    val chatRepository: ChatRepository
        get() = ServiceLocator.provideChatRepository(context)


    override fun onCreate() {
        super.onCreate()

    }
}