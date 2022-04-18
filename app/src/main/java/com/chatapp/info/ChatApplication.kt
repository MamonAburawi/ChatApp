package com.chatapp.info

import android.app.Application
import android.content.Context
import com.chatapp.info.di.*
import com.chatapp.info.repository.chat.ChatRepository
import com.chatapp.info.repository.message.MessageRepository
import com.chatapp.info.repository.user.UserRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module


class ChatApplication(): Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ChatApplication)
            modules(listOf(
                remoteDataBaseModule,
                viewModelModule,
                userRepoModule,
                messageRepoModule,
                chatRepoModule,
                chatDataBaseModule,
            ))
        }

    }
}