package com.chatapp.info.database.server

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference

class FirebaseDatabase() {

    fun readAll(path: CollectionReference) = path

    fun readOneDoc(path: DocumentReference) = path

    fun add(path: DocumentReference, data: Any) = path.set(data)

    fun remove(path: DocumentReference) = path.delete()

    fun removeAll(path: CollectionReference) = path

    fun updateField(path: DocumentReference,field: String,data: Any) = path.update(field,data)


}