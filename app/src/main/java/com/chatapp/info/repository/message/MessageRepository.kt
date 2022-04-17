package com.chatapp.info.repository.message

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import com.chatapp.info.data.Message
import com.chatapp.info.utils.ERR_UPLOAD
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import com.chatapp.info.utils.StoreDataStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import java.util.*

class MessageRepository(
    private val localMessageRepository: LocalMessageRepository,
    private val remoteMessageRepository: RemoteMessageRepository
) {

    companion object {
        private const val TAG = "MessageRepository"
    }

    suspend fun refreshMessages(chatId: String): StoreDataStatus? {
        Log.d(TAG,"update messages in room")
       return updateMessagesFromRemoteSource(chatId)
    }

    suspend fun deleteMultipleMessages(data: List<Message>) {
        localMessageRepository.deleteMultipleMessages(data)
    }

    fun observeMessage(): LiveData<Result<List<Message>>?> {
      return localMessageRepository.observeMessages()
    }

    suspend fun getMessagesByChatId(chatId: String): Result<List<Message>> {
        return localMessageRepository.getMessageByChatId(chatId)
    }

    fun observeMessagesOnLocalByChatId(chatId: String): LiveData<Result<List<Message>>?> {
        return localMessageRepository.observeMessagesByChatId(chatId)
    }

    fun observeMessagesOnRemoteByChatId(chatId: String, listener: (List<Message>) -> Unit) {
       return remoteMessageRepository.observeMessagesOnRemoteByChatId(chatId, listener)
    }

    suspend fun insertMultipleMessages(data: List<Message>) {
        localMessageRepository.insertMultipleMessages(data)
    }

    suspend fun getMessageById(messageId: String, forceUpdate: Boolean): Result<Message?> {
        if (forceUpdate) {
            updateMessageFromRemoteSource(messageId)
        }
        return localMessageRepository.getMessageById(messageId)
    }

    suspend fun insertMessage(message: Message): Result<Boolean> {
        return supervisorScope {
            val localRes = async {
                Log.d(TAG, "onInsertMessage: adding Message to local source")
                localMessageRepository.insertMessage(message)
            }
            val remoteRes = async {
                Log.d(TAG, "onInsertMessage: adding Message to remote source")
                remoteMessageRepository.insertMessage(message)
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

    suspend fun insertImages(imgList: List<Uri>): List<String> {
        var urlList = mutableListOf<String>()
        imgList.forEach label@{ uri ->
            val uniId = UUID.randomUUID().toString()
            val fileName = uniId + uri.lastPathSegment?.split("/")?.last()
            try {
                val downloadUrl = remoteMessageRepository.uploadImage(uri, fileName)
                urlList.add(downloadUrl.toString())
            } catch (e: Exception) {
                remoteMessageRepository.revertUpload(fileName)
                Log.d(TAG, "exception: message = $e")
                urlList = mutableListOf()
                urlList.add(ERR_UPLOAD)
                return@label
            }
        }
        return urlList
    }

    suspend fun updateMessage(message: Message): Result<Boolean> {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onUpdate: updating Message in remote source")
                remoteMessageRepository.updateMessage(message)
            }
            val localRes = async {
                Log.d(TAG, "onUpdate: updating Message in local source")
                localMessageRepository.updateMessage(message)
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

    suspend fun updateImages(newList: List<Uri>, oldList: List<String>): List<String> {
        var urlList = mutableListOf<String>()
        newList.forEach label@{ uri ->
            if (!oldList.contains(uri.toString())) {
                val uniId = UUID.randomUUID().toString()
                val fileName = uniId + uri.lastPathSegment?.split("/")?.last()
                try {
                    val downloadUrl = remoteMessageRepository.uploadImage(uri, fileName)
                    urlList.add(downloadUrl.toString())
                } catch (e: Exception) {
                    remoteMessageRepository.revertUpload(fileName)
                    Log.d(TAG, "exception: message = $e")
                    urlList = mutableListOf()
                    urlList.add(ERR_UPLOAD)
                    return@label
                }
            } else {
                urlList.add(uri.toString())
            }
        }
        oldList.forEach { imgUrl ->
            if (!newList.contains(imgUrl.toUri())) {
                remoteMessageRepository.deleteImage(imgUrl)
            }
        }
        return urlList
    }

    suspend fun deleteMessage(message: Message): Result<Boolean> {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onDelete: deleting Message from remote source")
                remoteMessageRepository.deleteMessage(message)
            }
            val localRes = async {
                Log.d(TAG, "onDelete: deleting Message from local source")
                localMessageRepository.deleteMessage(message)
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

    suspend fun deleteAllMessages() {
        localMessageRepository.deleteAllMessages()
    }

    suspend fun getLastMessage(chatId: String): Result<Message?> {
       return localMessageRepository.getLastMessage(chatId)
    }

    suspend fun deleteAllMessagesByChatId(chatId: String) {
        localMessageRepository.deleteAllMessagesByChatId(chatId)
    }

    private suspend fun updateMessageFromRemoteSource(messageId: String): StoreDataStatus? {
        var res: StoreDataStatus? = null
        try {
            val messageRemote = remoteMessageRepository.getMessageById(messageId)
            if (messageRemote is Success) {
                messageRemote.data?.let { remoteMessageRepository.insertMessage(it) }
                res = StoreDataStatus.DONE
            } else {
                res = StoreDataStatus.ERROR
                if (messageRemote is Error)
                    throw messageRemote.exception
            }
        } catch (e: Exception) {
            Log.d(TAG, "onUpdateMessagesFromRemoteSource: Exception occurred, ${e.message}")
        }
        return res
    }

    private suspend fun updateMessagesFromRemoteSource(chatId: String): StoreDataStatus? {
        var res: StoreDataStatus? = null
        try {
            val remoteMessages = remoteMessageRepository.getMessageByChatId(chatId)
            if (remoteMessages is Success) {
                Log.d(TAG, "messages list = ${remoteMessages.data.size}")
//                deleteAllMessagesByChatId(chatId)
                localMessageRepository.insertMultipleMessages(remoteMessages.data)
                res = StoreDataStatus.DONE
            } else {
                res = StoreDataStatus.ERROR
                if (remoteMessages is Error)
                    throw remoteMessages.exception
            }
        } catch (e: Exception) {
            Log.d(TAG, "onUpdateMessagesFromRemoteSource: Exception occurred, ${e.message}")
        }

        return res
    }


}