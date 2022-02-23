package com.chatapp.info.repository.server

import com.chatapp.info.data.Message
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.chatapp.info.database.server.FirebaseDatabase

class ChatRepositoryOnline() {
    private val database = FirebaseDatabase()

    suspend fun getMessages(path: CollectionReference) = database.readAll(path)

    suspend fun addMessage(path: DocumentReference, message: Message) = database.add(path,message)

    suspend fun deleteMessage(path: DocumentReference) = database.remove(path)

    suspend fun getChats(path: CollectionReference) = database.readAll(path)




    // todo set function of upload image here

    // todo set image of download image here

}