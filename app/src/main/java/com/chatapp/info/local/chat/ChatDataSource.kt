package com.chatapp.info.local.chat

import android.net.Uri
import androidx.lifecycle.LiveData
import com.chatapp.info.data.Chat
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.utils.Result
import com.chatapp.info.utils.Result.Success


interface ChatDataSource {

    fun observeChats(): LiveData<Result<List<Chat>>?>

    suspend fun getChats(userId: String): Result<List<Chat>?>

    suspend fun getChatById(chatId: String): Result<Chat?>

    suspend fun insertChat(chat: Chat)

    suspend fun updateChat(chat: Chat)


    suspend fun uploadImage(uri: Uri, fileName: String): Uri? {
        return null
    }

    fun revertUpload(fileName: String) {}

    fun deleteImage(imgUrl: String) {}

    suspend fun deleteChat(chat: Chat)

    suspend fun deleteAllChats(){}

    suspend fun insertMultipleChats(data: List<Chat>) {}

}