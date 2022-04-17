package com.chatapp.info.repository.message

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.chatapp.info.data.Message
import com.chatapp.info.local.api.MessageApi
import kotlinx.coroutines.Dispatchers
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import kotlinx.coroutines.withContext

class LocalMessageRepository (private val messageApi: MessageApi){

    private val ioDispatcher = Dispatchers.IO

    fun observeMessages(): LiveData<Result<List<Message>>?> {
        return try {
            Transformations.map(messageApi.observeMessages()) {
                Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Error(e)
            }
        }
    }

    fun observeMessagesByChatId(chatId: String): LiveData<Result<List<Message>>?> {
        return try {
            Transformations.map(messageApi.observeMessagesByChatId(chatId)) {
                Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Error(e)
            }
        }
    }

    suspend fun getMessageByChatId(chatId: String): Result<List<Message>> = withContext(ioDispatcher) {
        return@withContext try {
            Success(messageApi.getMessagesByChatId(chatId))
        } catch (e: Exception) {
            Error(e)
        }
    }

    suspend fun getMessageById(messageId: String): Result<Message?> = withContext(ioDispatcher) {
        return@withContext try {
            Success(messageApi.getMessageById(messageId))
        } catch (e: Exception) {
            Error(e)
        }
    }

    suspend fun getLastMessage(chatId: String): Result<Message?> = withContext(ioDispatcher) {
        return@withContext try {
            Success(messageApi.getLastMessage(chatId))
        } catch (e: Exception) {
            Error(e)
        }
    }

    suspend fun insertMessage(message: Message) {
        withContext(ioDispatcher){
            messageApi.insert(message)
        }
    }

    suspend fun updateMessage(message: Message) {
        withContext(ioDispatcher){
            messageApi.update(message)
        }
    }

    suspend fun deleteMessage(message: Message) {
        withContext(ioDispatcher){
            messageApi.deleteMessageById(message.messageId)
        }
    }

    suspend fun deleteAllMessagesByChatId(chatId: String) {
        withContext(ioDispatcher){
            messageApi.deleteMessageById(chatId)
        }
    }

    suspend fun deleteAllMessages() {
      withContext(ioDispatcher){
          messageApi.deleteAllMessages()
      }
    }

    suspend fun insertMultipleMessages(data:List<Message>){
        withContext(ioDispatcher){
            messageApi.insertMultipleMessages(data)
        }
    }

    suspend fun deleteMultipleMessages(data: List<Message>) {
        withContext(ioDispatcher){
            data.forEach { message ->
                messageApi.deleteMessageById(message.messageId)
            }
        }
    }

}