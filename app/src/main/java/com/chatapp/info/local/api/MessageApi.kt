package com.chatapp.info.local.api

import androidx.lifecycle.LiveData
import androidx.room.*
import com.chatapp.info.data.Message
import com.chatapp.info.data.User

@Dao
interface MessageApi {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleMessages(messages: List<Message>)

    @Update
    suspend fun update(message: Message)

    @Query("select * from messages where chatId = :chatId in (select max(messageId) from messages)")
    suspend fun getLastMessage(chatId: String): Message?

    @Query("SELECT * FROM messages WHERE chatId = :chatId ")
    suspend fun getMessagesByChatId(chatId: String): List<Message>

    @Query("SELECT * FROM messages")
    fun observeMessages(): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE chatId = :chatId")
    fun observeMessagesByChatId(chatId: String): LiveData<List<Message>>

    @Query("SELECT * FROM messages WHERE messageId = :messageId")
    suspend fun getMessageById(messageId: String): Message?

    @Query("DELETE FROM messages WHERE messageId = :messageId")
    suspend fun deleteMessageById(messageId: String): Int

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()


}