package com.chatapp.info.repository.message

import android.net.Uri
import androidx.lifecycle.LiveData
import com.chatapp.info.data.Message
import com.chatapp.info.utils.StoreDataStatus
import com.chatapp.info.utils.Result

interface MessageRepoInterface {

    suspend fun refreshMessages(chatId: String): StoreDataStatus?
    fun observeMessage(): LiveData<Result<List<Message>>?>
    suspend fun getMessagesByChatId(chatId: String): Result<List<Message>>
    fun observeMessagesOnLocalByChatId(chatId: String): LiveData<Result<List<Message>>?>
    fun observeMessagesOnRemoteByChatId(chatId: String,listener:(List<Message>) -> Unit)
    suspend fun insertMultipleMessages(data: List<Message>)
    suspend fun deleteMultipleMessages(data: List<Message>)
    suspend fun getMessageById(messageId: String, forceUpdate: Boolean = false): Result<Message?>
    suspend fun insertMessage(message: Message): Result<Boolean>
    suspend fun insertImages(imgList: List<Uri>): List<String>
    suspend fun updateMessage(message: Message): Result<Boolean>
    suspend fun updateImages(newList: List<Uri>, oldList: List<String>): List<String>
    suspend fun deleteMessage(message: Message): Result<Boolean>
    suspend fun deleteAllMessagesByChatId(chatId: String){}
    suspend fun deleteAllMessages()

    suspend fun getLastMessage(chatId: String): Result<Message?>



}