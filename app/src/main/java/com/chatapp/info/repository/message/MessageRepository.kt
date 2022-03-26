package com.chatapp.info.repository.message

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import com.chatapp.info.data.Message
import com.chatapp.info.local.message.MessageDataSource
import com.chatapp.info.remote.MessageRemoteDataSource
import com.chatapp.info.utils.ERR_UPLOAD
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import com.chatapp.info.utils.StoreDataStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import java.util.*

class MessageRepository(
    private val messageLocalDataSource: MessageDataSource,
    private val messageRemoteDataSource: MessageRemoteDataSource
) : MessageRepoInterface {

    companion object {
        private const val TAG = "MessageRepository"
    }

    override suspend fun refreshMessages(chatId: String): StoreDataStatus? {
        Log.d(TAG,"update messages in room")
       return updateMessagesFromRemoteSource(chatId)
    }


    override suspend fun deleteMultipleMessages(data: List<Message>) {
        messageLocalDataSource.deleteMultipleMessages(data)
    }

    override fun observeMessage(): LiveData<Result<List<Message>>?> {
      return messageLocalDataSource.observeMessages()
    }

    override suspend fun getMessagesByChatId(chatId: String): Result<List<Message>> {
        return messageLocalDataSource.getMessageByChatId(chatId)
    }

    override fun observeMessagesOnLocalByChatId(chatId: String): LiveData<Result<List<Message>>?> {
        return messageLocalDataSource.observeMessagesByChatId(chatId)
    }

    override fun observeMessagesOnRemoteByChatId(chatId: String, listener: (List<Message>) -> Unit) {
       return messageRemoteDataSource.observeMessagesOnRemoteByChatId(chatId, listener)
    }

    override suspend fun insertMultipleMessages(data: List<Message>) {
        messageLocalDataSource.insertMultipleMessages(data)
    }


    override suspend fun getMessageById(messageId: String, forceUpdate: Boolean): Result<Message?> {
        if (forceUpdate) {
            updateMessageFromRemoteSource(messageId)
        }
        return messageLocalDataSource.getMessageById(messageId)
    }


    override suspend fun insertMessage(message: Message): Result<Boolean> {
        return supervisorScope {
            val localRes = async {
                Log.d(TAG, "onInsertMessage: adding Message to local source")
                messageLocalDataSource.insertMessage(message)
            }
            val remoteRes = async {
                Log.d(TAG, "onInsertMessage: adding Message to remote source")
                messageRemoteDataSource.insertMessage(message)
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

    override suspend fun insertImages(imgList: List<Uri>): List<String> {
        var urlList = mutableListOf<String>()
        imgList.forEach label@{ uri ->
            val uniId = UUID.randomUUID().toString()
            val fileName = uniId + uri.lastPathSegment?.split("/")?.last()
            try {
                val downloadUrl = messageRemoteDataSource.uploadImage(uri, fileName)
                urlList.add(downloadUrl.toString())
            } catch (e: Exception) {
                messageRemoteDataSource.revertUpload(fileName)
                Log.d(TAG, "exception: message = $e")
                urlList = mutableListOf()
                urlList.add(ERR_UPLOAD)
                return@label
            }
        }
        return urlList
    }


    override suspend fun updateMessage(message: Message): Result<Boolean> {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onUpdate: updating Message in remote source")
                messageRemoteDataSource.updateMessage(message)
            }
            val localRes = async {
                Log.d(TAG, "onUpdate: updating Message in local source")
                messageLocalDataSource.updateMessage(message)
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

    override suspend fun updateImages(newList: List<Uri>, oldList: List<String>): List<String> {
        var urlList = mutableListOf<String>()
        newList.forEach label@{ uri ->
            if (!oldList.contains(uri.toString())) {
                val uniId = UUID.randomUUID().toString()
                val fileName = uniId + uri.lastPathSegment?.split("/")?.last()
                try {
                    val downloadUrl = messageRemoteDataSource.uploadImage(uri, fileName)
                    urlList.add(downloadUrl.toString())
                } catch (e: Exception) {
                    messageRemoteDataSource.revertUpload(fileName)
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
                messageRemoteDataSource.deleteImage(imgUrl)
            }
        }
        return urlList
    }

    override suspend fun deleteMessage(message: Message): Result<Boolean> {
        return supervisorScope {
            val remoteRes = async {
                Log.d(TAG, "onDelete: deleting Message from remote source")
                messageRemoteDataSource.deleteMessage(message)
            }
            val localRes = async {
                Log.d(TAG, "onDelete: deleting Message from local source")
                messageLocalDataSource.deleteMessage(message)
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

    override suspend fun deleteAllMessages() {
        messageLocalDataSource.deleteAllMessages()
    }

    override suspend fun getLastMessage(chatId: String): Result<Message?> {
       return messageLocalDataSource.getLastMessage(chatId)
    }

    override suspend fun deleteAllMessagesByChatId(chatId: String) {
        messageLocalDataSource.deleteAllMessagesByChatId(chatId)
    }


    private suspend fun updateMessageFromRemoteSource(messageId: String): StoreDataStatus? {
        var res: StoreDataStatus? = null
        try {
            val messageRemote = messageRemoteDataSource.getMessageById(messageId)
            if (messageRemote is Success) {
                messageRemote.data?.let { messageRemoteDataSource.insertMessage(it) }
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
            val remoteMessages = messageRemoteDataSource.getMessageByChatId(chatId)
            if (remoteMessages is Success) {
                Log.d(TAG, "messages list = ${remoteMessages.data.size}")
//                deleteAllMessagesByChatId(chatId)
                messageLocalDataSource.insertMultipleMessages(remoteMessages.data)
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