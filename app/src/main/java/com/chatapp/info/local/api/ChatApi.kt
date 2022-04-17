package com.chatapp.info.local.api

import androidx.lifecycle.LiveData
import androidx.room.*
import com.chatapp.info.data.Chat


@Dao
interface ChatApi {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: Chat)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleChats(chats: List<Chat>)

    @Update(entity = Chat::class)
    suspend fun update(chat: Chat)

    @Query("SELECT * FROM chat")
    suspend fun getChats(): List<Chat>

    @Query("SELECT * FROM chat WHERE chatId = :chatId ")
    suspend fun getChatById(chatId: String): Chat?

    @Query("SELECT * FROM chat")
    fun observeChats(): LiveData<List<Chat>>

    @Query("DELETE FROM chat WHERE chatId = :chatId")
    suspend fun deleteChat(chatId: String): Int

    @Query("DELETE FROM chat WHERE chatId = :chatId")
    suspend fun deleteMutlipleChats(chatId: String)

    @Query("DELETE FROM chat")
    suspend fun deleteAllChats()


}