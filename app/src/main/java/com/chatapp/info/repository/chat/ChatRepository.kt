package com.chatapp.info.repository.chat

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import com.chatapp.info.data.Chat
import com.chatapp.info.data.Message
import com.chatapp.info.local.chat.ChatDataSource
import com.chatapp.info.remote.ChatRemoteDataSource
import com.chatapp.info.screens.chats.Chats
import com.chatapp.info.utils.ERR_UPLOAD
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import com.chatapp.info.utils.StoreDataStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import java.util.*

class ChatRepository(
    private val chatLocalDataSource: ChatDataSource,
    private val chatRemoteDataSource: ChatRemoteDataSource
) : ChatRepoInterface {

    companion object {
        private const val TAG = "ChatRepository"
    }

    override suspend fun refreshChats(userId: String): StoreDataStatus? {
        Log.d(TAG,"update chats in room")
       return updateChatsFromRemote(userId)
    }


    override suspend fun deleteMultipleChats(data: List<Chat>) {
        data.forEach {
            deleteChat(it)
        }
    }

    override fun observeChats(): LiveData<Result<List<Chat>>?> {
      return chatLocalDataSource.observeChats()
    }

    override suspend fun getChats(userId: String): Result<List<Chat>?> {
        return chatLocalDataSource.getChats(userId)
    }

    override suspend fun updateChat(chat: Chat): Result<Boolean> {
        return supervisorScope {
            val localRes = async {
                Log.d(TAG, "onUpdateChat: update Chat to local source")
                chatLocalDataSource.updateChat(chat)
            }
            val remoteRes = async {
                Log.d(TAG, "onUpdateChat: update Chat to remote source")
                chatRemoteDataSource.updateChat(chat)
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

    override suspend fun getChatById(chatId: String, forceUpdate: Boolean): Result<Chat?> {
        return chatLocalDataSource.getChatById(chatId)
    }

    override fun observeChatsOnLocal(chatId: String): LiveData<Result<List<Chat>>?> {
        return chatLocalDataSource.observeChats()
    }

    override fun observeChatsOnRemote(chatId: String, listener: (List<Chat>) -> Unit) {
       return chatRemoteDataSource.observeChatsOnRemoteByChatId(chatId, listener)
    }

    override suspend fun insertMultipleChats(data: List<Chat>) {
        chatLocalDataSource.insertMultipleChats(data)
    }



    override suspend fun insertChat(chat: Chat): Result<Boolean> {
        return supervisorScope {
//            val localRes = async {
//                Log.d(TAG, "onInsertChat: adding Chat to local source")
//                chatLocalDataSource.insertChat(chat)
//            }
            val remoteRes = async {
                Log.d(TAG, "onInsertChat: adding Message to remote source")
                chatRemoteDataSource.insertChat(chat)
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

    override suspend fun insertImage(img: Uri): String {
        var downloadUrl = ""
            val uniId = UUID.randomUUID().toString()
            val fileName = uniId + img.lastPathSegment?.split("/")?.last()
            try {
                downloadUrl = chatRemoteDataSource.uploadImage(img, fileName).toString()
            } catch (e: Exception) {
                chatRemoteDataSource.revertUpload(fileName)
                Log.d(TAG, "exception: chat = $e")
        }
        return downloadUrl
    }


    override suspend fun deleteChat(chat: Chat): Result<Boolean> {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onDelete: deleting Chat from remote source")
                chatRemoteDataSource.deleteChat(chat)
            }
            val localRes = async {
                Log.d(TAG, "onDelete: deleting Chat from local source")
                chatLocalDataSource.deleteChat(chat)
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


    override suspend fun deleteAllChats() {
        chatLocalDataSource.deleteAllChats()
    }

    private suspend fun updateChatsFromRemote(userId: String): StoreDataStatus? {
        var res: StoreDataStatus? = null
        try {
            res = StoreDataStatus.LOADING
            val remoteMessages = chatRemoteDataSource.getChats(userId)
            if (remoteMessages is Success) {
                val data = remoteMessages.data
                if (data != null){
                    Log.d(TAG, "chats list = ${data.size}")
                    chatLocalDataSource.insertMultipleChats(data)
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