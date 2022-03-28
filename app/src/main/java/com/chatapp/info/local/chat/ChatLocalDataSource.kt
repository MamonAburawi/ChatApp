package com.chatapp.info.local.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.chatapp.info.data.Chat
import com.chatapp.info.data.Message
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success
import com.chatapp.info.utils.Result.Error
import kotlinx.coroutines.withContext

class ChatLocalDataSource internal constructor(
    private val chatDoa: ChatDoa,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
):ChatDataSource{


    override fun observeChats(): LiveData<Result<List<Chat>>?> {
        return try {
            Transformations.map(chatDoa.observeChats()) {
                Success(it)
            }
        } catch (e: Exception) {
            Transformations.map(MutableLiveData(e)) {
                Error(e)
            }
        }
    }

    override suspend fun getChats(userId: String): Result<List<Chat>?> = withContext(ioDispatcher) {
        return@withContext try {
            Success(chatDoa.getChats())
        }catch (ex: Exception){
            Error(ex)
        }
    }


    override suspend fun getChatById(chatId: String): Result<Chat?> = withContext(ioDispatcher) {
        return@withContext try {
            Success(chatDoa.getChatById(chatId))
        } catch (e: Exception) {
            Error(e)
        }
    }


    override suspend fun insertChat(chat: Chat) {
        withContext(ioDispatcher){
            chatDoa.insert(chat)
        }
    }


    override suspend fun updateChat(chat: Chat) {
        withContext(ioDispatcher){
            chatDoa.update(chat)
        }
    }

    override suspend fun deleteChat(chat: Chat) {
        withContext(ioDispatcher){
            chatDoa.deleteChat(chat.chatId)
        }
    }



    override suspend fun deleteAllChats() {
      withContext(ioDispatcher){
          chatDoa.deleteAllChats()
      }
    }

    override suspend fun insertMultipleChats(data:List<Chat>){
        withContext(ioDispatcher){
            chatDoa.insertMultipleChats(data)
        }
    }




}