package com.chatapp.info.repository.chat

import android.net.Uri
import androidx.lifecycle.LiveData
import com.chatapp.info.data.Chat
import com.chatapp.info.utils.StoreDataStatus
import com.chatapp.info.utils.Result

interface ChatRepoInterface {

    suspend fun refreshChats(userId: String): StoreDataStatus?

    fun observeChats(): LiveData<Result<List<Chat>>?>

    suspend fun getChats(userId: String): Result<List<Chat>?>

    fun observeChatsOnLocal(chatId: String): LiveData<Result<List<Chat>>?>

    fun observeChatsOnRemote(chatId: String,listener:(List<Chat>) -> Unit)

    suspend fun insertMultipleChats(data: List<Chat>)

    suspend fun deleteMultipleChats(data: List<Chat>)

    suspend fun getChatById(chatId: String, forceUpdate: Boolean = false): Result<Chat?>

    suspend fun insertChat(chat: Chat): Result<Boolean>

    suspend fun insertImage(img: Uri): String

    suspend fun deleteChat(chat: Chat): Result<Boolean>

    suspend fun updateChat(chat: Chat): Result<Boolean>

    suspend fun deleteAllChats(){}


}