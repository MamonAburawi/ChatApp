package com.chatapp.info

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.chatapp.info.local.ChatAppDataBase
import com.chatapp.info.local.chat.ChatDataSource
import com.chatapp.info.local.chat.ChatLocalDataSource
import com.chatapp.info.local.message.MessageDataSource
import com.chatapp.info.local.message.MessageLocalDataSource
import com.chatapp.info.local.user.UserDataSource
import com.chatapp.info.local.user.UserLocalDataSource
import com.chatapp.info.remote.ChatRemoteDataSource
import com.chatapp.info.remote.MessageRemoteDataSource
import com.chatapp.info.remote.UserRemoteDataSource
import com.chatapp.info.repository.chat.ChatRepoInterface
import com.chatapp.info.repository.chat.ChatRepository
import com.chatapp.info.repository.message.MessageRepoInterface
import com.chatapp.info.repository.message.MessageRepository
import com.chatapp.info.repository.user.UserRepoInterface
import com.chatapp.info.repository.user.UserRepository
import com.chatapp.info.utils.ChatAppSessionManager


object ServiceLocator {
	private var database: ChatAppDataBase? = null
	private val lock = Any()

	@Volatile
	var userRepository: UserRepoInterface? = null
		@VisibleForTesting set

	@Volatile
	var messageRepository: MessageRepoInterface? = null
		@VisibleForTesting set

	@Volatile
	var chatRepository: ChatRepoInterface? = null
		@VisibleForTesting set

	fun provideUserRepository(context: Context): UserRepoInterface {
		synchronized(this) {
			return userRepository ?: createUserRepository(context)
		}
	}

	fun provideMessageRepository(context: Context): MessageRepoInterface {
		synchronized(this) {
			return messageRepository ?: createMessageRepository(context)
		}
	}

	fun provideChatRepository(context: Context): ChatRepoInterface {
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

	private fun createMessageRepository(context: Context): MessageRepoInterface {
		val newRepo = MessageRepository(createMessageLocalDataSource(context), MessageRemoteDataSource() )
		messageRepository = newRepo
		return newRepo
	}

	private fun createChatRepository(context: Context): ChatRepoInterface {
		val newRepo = ChatRepository(createChatLocalDataSource(context), ChatRemoteDataSource() )
		chatRepository = newRepo
		return newRepo
	}


	private fun createUserRepository(context: Context): UserRepoInterface {
		val appSession = ChatAppSessionManager(context.applicationContext)
		val newRepo = UserRepository(createUserLocalDataSource(context), UserRemoteDataSource(), appSession)
		userRepository = newRepo
		return newRepo
	}

	private fun createMessageLocalDataSource(context: Context): MessageDataSource {
		val database = database ?: ChatAppDataBase.getInstance(context.applicationContext)
		return MessageLocalDataSource(database.messagesDao())
	}

	private fun createUserLocalDataSource(context: Context): UserDataSource {
		val database = database ?: ChatAppDataBase.getInstance(context.applicationContext)
		return UserLocalDataSource(database.userDao())
	}

	private fun createChatLocalDataSource(context: Context): ChatDataSource {
		val database = database ?: ChatAppDataBase.getInstance(context.applicationContext)
		return ChatLocalDataSource(database.chatDao())
	}
}