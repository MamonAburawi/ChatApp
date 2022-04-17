package com.chatapp.info

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.chatapp.info.local.ChatAppDataBase
import com.chatapp.info.repository.chat.LocalChatRepository
import com.chatapp.info.repository.message.LocalMessageRepository
import com.chatapp.info.repository.user.LocalUserRepository
import com.chatapp.info.repository.chat.RemoteChatRepository
import com.chatapp.info.repository.message.RemoteMessageRepository
import com.chatapp.info.repository.user.RemoteUserRepository
import com.chatapp.info.repository.chat.ChatRepository
import com.chatapp.info.repository.message.MessageRepository
import com.chatapp.info.repository.user.UserRepository
import com.chatapp.info.utils.ChatAppSessionManager


object ServiceLocator {
	private var database: ChatAppDataBase? = null
	private val lock = Any()

	@Volatile
	var userRepository: UserRepository? = null
		@VisibleForTesting set

	@Volatile
	var messageRepository: MessageRepository? = null
		@VisibleForTesting set

	@Volatile
	var chatRepository: ChatRepository? = null
		@VisibleForTesting set

	fun provideUserRepository(context: Context): UserRepository {
		synchronized(this) {
			return userRepository ?: createUserRepository(context)
		}
	}

	fun provideMessageRepository(context: Context): MessageRepository {
		synchronized(this) {
			return messageRepository ?: createMessageRepository(context)
		}
	}

	fun provideChatRepository(context: Context): ChatRepository {
		synchronized(this) {
			return chatRepository ?: createChatRepository(context)
		}
	}


	@VisibleForTesting
	fun resetRepository() {
		synchronized(lock) {
			database?.apply {
				clearAllTables()
				close()
			}
			database = null
			userRepository = null
		}
	}

	private fun createMessageRepository(context: Context): MessageRepository {
		val database = database ?: ChatAppDataBase.getInstance(context.applicationContext)
		val localRepository = LocalMessageRepository(database.messagesDao())
		val newRepo = MessageRepository(localRepository, RemoteMessageRepository() )
		messageRepository = newRepo
		return newRepo
	}

	private fun createChatRepository(context: Context): ChatRepository {
		val database = database ?: ChatAppDataBase.getInstance(context.applicationContext)
		val localRepository =  LocalChatRepository(database.localChatApi())
		val newRepo = ChatRepository(localRepository, RemoteChatRepository())
		chatRepository = newRepo
		return newRepo
	}


	private fun createUserRepository(context: Context): UserRepository {
		val database = database ?: ChatAppDataBase.getInstance(context.applicationContext)
		val localRepository = LocalUserRepository(database.userDao())
		val appSession = ChatAppSessionManager(context.applicationContext)
		val newRepo = UserRepository(localRepository, RemoteUserRepository(), appSession)
		userRepository = newRepo
		return newRepo
	}



}