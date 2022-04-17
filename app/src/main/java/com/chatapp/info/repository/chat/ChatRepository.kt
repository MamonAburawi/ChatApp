package com.chatapp.info.repository.chat

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import com.chatapp.info.data.Chat
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import com.chatapp.info.utils.StoreDataStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import java.util.*

class ChatRepository(
    private val chatLocalRepository: LocalChatRepository,
    private val chatRemoteRepository: RemoteChatRepository
) {

    companion object {
        private const val TAG = "ChatRepository"
    }

    suspend fun refreshChats(userId: String): StoreDataStatus? {
        Log.d(TAG,"update chats in room")
       return updateChatsFromRemote(userId)
    }

    suspend fun deleteMultipleChats(data: List<Chat>) {
        data.forEach {
            deleteChat(it)
        }
    }

    fun observeChats(): LiveData<Result<List<Chat>>?> {
      return chatLocalRepository.observeChats()
    }

    suspend fun getChats(userId: String): Result<List<Chat>?> {
        return chatLocalRepository.getChats(userId)
    }

    suspend fun updateChat(chat: Chat): Result<Boolean> {
        return supervisorScope {
            val localRes = async {
                Log.d(TAG, "onUpdateChat: update Chat to local source")
                chatLocalRepository.updateChat(chat)
            }
            val remoteRes = async {
                Log.d(TAG, "onUpdateChat: update Chat to remote source")
                chatRemoteRepository.updateChat(chat)
            }
            try {
                localRes.await()
                remoteRes.await()
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    suspend fun getChatById(chatId: String, forceUpdate: Boolean): Result<Chat?> {
        return chatLocalRepository.getChatById(chatId)
    }

    fun observeChatsOnLocal(chatId: String): LiveData<Result<List<Chat>>?> {
        return chatLocalRepository.observeChats()
    }

    fun observeChatsOnRemote(chatId: String, listener: (List<Chat>) -> Unit) {
       return chatRemoteRepository.observeChatsOnRemoteByChatId(chatId, listener)
    }

    suspend fun insertMultipleChats(data: List<Chat>) {
        chatLocalRepository.insertMultipleChats(data)
    }

    suspend fun insertChat(chat: Chat): Result<Boolean> {
        return supervisorScope {
//            val localRes = async {
//                Log.d(TAG, "onInsertChat: adding Chat to local source")
//                chatLocalDataSource.insertChat(chat)
//            }
            val remoteRes = async {
                Log.d(TAG, "onInsertChat: adding Message to remote source")
                chatRemoteRepository.insertChat(chat)
            }
            try {
//                localRes.await()
                remoteRes.await()
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    suspend fun insertImage(img: Uri): String {
        var downloadUrl = ""
            val uniId = UUID.randomUUID().toString()
            val fileName = uniId + img.lastPathSegment?.split("/")?.last()
            try {
                downloadUrl = chatRemoteRepository.uploadImage(img, fileName).toString()
            } catch (e: Exception) {
                chatRemoteRepository.revertUpload(fileName)
                Log.d(TAG, "exception: chat = $e")
        }
        return downloadUrl
    }

    suspend fun deleteChat(chat: Chat): Result<Boolean> {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onDelete: deleting Chat from remote source")
                chatRemoteRepository.deleteChat(chat)
            }
            val localRes = async {
                Log.d(TAG, "onDelete: deleting Chat from local source")
                chatLocalRepository.deleteChat(chat)
            }
            try {
                remoteRes.await()
                localRes.await()
                Success(true)
            } catch (e: Exception) {
                Error(e)
            }
        }
    }

    suspend fun deleteAllChats() {
        chatLocalRepository.deleteAllChats()
    }

    private suspend fun updateChatsFromRemote(userId: String): StoreDataStatus? {
        var res: StoreDataStatus? = null
        try {
            res = StoreDataStatus.LOADING
            val remoteMessages = chatRemoteRepository.getChats(userId)
            if (remoteMessages is Success) {
                val data = remoteMessages.data
                if (data != null){
                    Log.d(TAG, "chats list = ${data.size}")
                    chatLocalRepository.insertMultipleChats(data)
                    res = StoreDataStatus.DONE
                }

            } else {
                res = StoreDataStatus.ERROR
                if (remoteMessages is Error)
                    throw remoteMessages.exception
            }
        } catch (e: Exception) {
            Log.d(TAG, "onUpdateChatsFromRemoteSource: Exception occurred, ${e.message}")
        }

        return res
    }


}