package com.chatapp.info.local.message

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.chatapp.info.data.Message
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MessageLocalDataSource internal constructor(
    private val messageDao: MessageDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
):MessageDataSource{



    override fun observeMessages(): LiveData<Result<List<Message>>?> {
        return try {
            Transformations.map(messageDao.observeMessages()) {
                Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Error(e)
            }
        }
    }

    override fun observeMessagesByChatId(chatId: String): LiveData<Result<List<Message>>?> {
        return try {
            Transformations.map(messageDao.observeMessagesByChatId(chatId)) {
                Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Error(e)
            }
        }
    }

    override suspend fun getMessageByChatId(chatId: String): Result<List<Message>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(messageDao.getMessagesByChatId(chatId))
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun getMessageById(messageId: String): Result<Message?> = withContext(ioDispatcher) {
        return@withContext try {
            Success(messageDao.getMessageById(messageId))
        } catch (e: Exception) {
            Error(e)
        }
    }

    override suspend fun insertMessage(message: Message) {
        withContext(ioDispatcher){
            messageDao.insert(message)
        }
    }


    override suspend fun updateMessage(message: Message) {
        withContext(ioDispatcher){
            messageDao.update(message)
        }
    }

    override suspend fun deleteMessage(message: Message) {
        withContext(ioDispatcher){
            messageDao.deleteMessageById(message.messageId)
        }
    }


    override suspend fun deleteAllMessagesByChatId(chatId: String) {
        withContext(ioDispatcher){
            messageDao.deleteMessageById(chatId)
        }
    }

    override suspend fun deleteAllMessages() {
      withContext(ioDispatcher){
          messageDao.deleteAllMessages()
      }
    }

    override suspend fun insertMultipleMessages(data:List<Message>){
        withContext(ioDispatcher){
            messageDao.insertMultipleMessages(data)
        }
    }

    override suspend fun deleteMultipleMessages(data: List<Message>) {
        withContext(ioDispatcher){
            data.forEach { message ->
                messageDao.deleteMessageById(message.messageId)
            }
        }
    }


}