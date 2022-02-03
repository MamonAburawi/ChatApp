//package com.chatapp.info.dao
//
//import androidx.lifecycle.LiveData
//import androidx.room.*
//import com.chatapp.info.data.Message
//import com.chatapp.info.data.MessageWithUser
//import com.chatapp.info.data.TextWithTime
//
//
//@Dao
//interface MessageDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertMessages( messages: Message)
//
//    @Update
//    suspend fun updateMessages(vararg messages: Message)
//
//    @Delete
//    suspend fun deleteMessages(vararg messages: Message)
//
//    @Query("SELECT * FROM messages")
//    suspend  fun loadAllMessages(): List<Message>
//
//    @Query("SELECT text,time FROM messages")
//    suspend fun loadTextsAndTimes(): List<TextWithTime>
//
//    @Query("SELECT * FROM messages INNER JOIN users on users.user_id=messages.recipient_id")
//    suspend fun loadMessagesAndUsers(): List<MessageWithUser>
//
//    @Query("SELECT * FROM messages WHERE recipient_id=:recipientId AND sender_id=:senderId")
//     fun loadMessagesFromUser(senderId: String, recipientId: String): List<Message>
//
//
//    @Query("SELECT * FROM messages WHERE recipient_id=:userId AND time>=:time")
//    suspend fun loadMessagesFromUserAfterTime(userId: String, time: Long):
//                List<Message>
//
//
//}