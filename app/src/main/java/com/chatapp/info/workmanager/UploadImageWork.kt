package com.chatapp.info.workmanager

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UploadImageWork(context : Context, paramsWorker: WorkerParameters) : CoroutineWorker(context,paramsWorker) {

    private val storage = FirebaseStorage.getInstance()

    private val image = inputData.getByte("image",0)!!


    private val imagePath by lazy {
        val imageId = inputData.getString("image_id")!!
        val chatId = inputData.getString("chat_id")!!
        storage.reference.child("images/$chatId/$imageId")
    }


    override suspend fun doWork(): Result {

        imagePath.putFile(image.toString().toUri())


        return Result.success()
    }
}