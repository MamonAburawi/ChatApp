package com.chatapp.info.repository.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.chatapp.info.data.Chat
import com.chatapp.info.local.api.ChatApi
import kotlinx.coroutines.Dispatchers
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import kotlinx.coroutines.withContext

class LocalChatRepository (private val chatApi: ChatApi) {

    private val ioDispatcher = Dispatchers.IO

     fun observeChats(): LiveData<Result<List<Chat>>?> {
        return try {
            Transformations.map(chatApi.observeChats()) {
                Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Error(e)
            }
        }
    }

     suspend fun getChats(userId: String): Result<List<Chat>?> = withContext(ioDispatcher) {
        return@withContext try {
            Success(chatApi.getChats())
        }catch (ex: Exception){
            Error(ex)
        }
    }

     suspend fun getChatById(chatId: String): Result<Chat?> = withContext(ioDispatcher) {
        return@withContext try {
            Success(chatApi.getChatById(chatId))
        } catch (e: Exception) {
            Error(e)
        }
    }

     suspend fun insertChat(chat: Chat) {
        withContext(ioDispatcher){
            chatApi.insert(chat)
        }
    }

     suspend fun updateChat(chat: Chat) {
        withContext(ioDispatcher){
            chatApi.update(chat)
        }
    }

     suspend fun deleteChat(chat: Chat) {
        withContext(ioDispatcher){
            chatApi.deleteChat(chat.chatId)
        }
    }

     suspend fun deleteAllChats() {
      withContext(ioDispatcher){
          chatApi.deleteAllChats()
      }
    }

     suspend fun insertMultipleChats(data:List<Chat>){
        withContext(ioDispatcher){
            chatApi.insertMultipleChats(data)
        }
    }



}