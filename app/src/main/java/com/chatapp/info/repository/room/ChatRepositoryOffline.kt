//package com.chatapp.info.repository.room
//
//import com.chatapp.info.data.Message
//
//
///**
//    chat repository has function to handle with the server and room in the same time..
// **/
//
//
//class ChatRepositoryOffline(private val messageDao: MessageDao) {
//
//    suspend fun loadAllMessages() = messageDao.loadAllMessages()
//
//    suspend fun insertMessages( messages: Message){
//        messageDao.insertMessages(messages)
//    }
//
//    suspend fun updateMessages(vararg messages: Message){
//        messageDao.updateMessages(*messages)
//    }
//
//    suspend fun deleteMessages(vararg messages: Message){
//        messageDao.deleteMessages(*messages)
//    }
//
//    suspend fun loadTextsAndTimes() = messageDao.loadTextsAndTimes()
//
//     suspend fun loadMessagesFromUser(senderId: String,recipientId: String) = messageDao.loadMessagesFromUser(senderId, recipientId)
//
//    suspend fun loadMessagesAndUsers() = messageDao.loadMessagesAndUsers()
//
//    suspend fun loadMessagesFromUserAfterTime(userId: String, time: Long) =
//        messageDao.loadMessagesFromUserAfterTime(userId, time)
//
//}