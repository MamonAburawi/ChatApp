package com.chatapp.info.local.message

import android.net.Uri
import androidx.lifecycle.LiveData
import com.chatapp.info.data.Chat
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success


interface MessageDataSource {

    fun observeMessages(): LiveData<Result<List<Message>>?>

    fun observeMessagesByChatId(chatId: String): LiveData<Result<List<Message>>?>

    suspend fun getMessageByChatId(chatId: String): Result<List<Message>>

    suspend fun refreshMessages() {}

    suspend fun getMessageById(messageId: String): Result<Message?>

    suspend fun getLastMessage(chatId: String): Result<Message?>

    suspend fun insertMessage(message: Message)

    suspend fun updateMessage(message: Message)

    suspend fun getAllChatsByUserId(userId: String): Result<List<Chat>> {
        return Success(emptyList())
    }

    suspend fun uploadImage(uri: Uri, fileName: String): Uri? {
        return null
    }

    fun revertUpload(fileName: String) {}

    fun deleteImage(imgUrl: String) {}

    suspend fun deleteMessage(message: Message)

    suspend fun deleteAllMessages(){}

    suspend fun deleteAllMessagesByChatId(chatId: String)

    suspend fun insertMultipleMessages(data: List<Message>) {}

    suspend fun deleteMultipleMessages(data: List<Message>){}
}