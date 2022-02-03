package com.chatapp.info.repository.server

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.auth.User
import com.chatapp.info.database.server.FirebaseDatabase

class UserRepositoryOnline() {

    private val database = FirebaseDatabase()

    suspend fun getUsers(path: CollectionReference) = database.readAll(path)

    suspend fun getUser(path: DocumentReference) = database.readOneDoc(path)

    suspend fun updateUserData(path: DocumentReference,field: String , data: Any) = database.updateField(path,field, data)

    suspend fun addNewUser(path: DocumentReference,user: Any) = database.add(path,user)
}