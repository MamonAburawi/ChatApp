package com.chatapp.info.workmanager

import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class UploadImageWork(val context : Context, paramsWorker: WorkerParameters) : CoroutineWorker(context,paramsWorker) {
    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
    }

//    companion object{
//        const val TAG = "UploadImage"
//    }
//
//    private val storage = FirebaseStorage.getInstance()
//    private val image = inputData.getString("image")!!
//
//    private val imagePath by lazy {
//        val chatId = inputData.getString("chat_id")!!
//        val imageId = inputData.getString("image_id")!!
//        storage.reference.child("images/$chatId/$imageId")
//    }
//
//
//    override suspend fun doWork(): Result {
//
//        val selectedImageBmp = MediaStore.Images.Media.getBitmap(context.contentResolver, image.toUri())
//        val outputStream = ByteArrayOutputStream()
//        selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)
//        val selectedImageBytes = outputStream.toByteArray()
//
//
//        imagePath.putBytes(selectedImageBytes)
//            .addOnSuccessListener {
//            Log.i(TAG,"image is uploaded")
//        }
//            .addOnFailureListener {
//            Log.i(TAG,"error upload: ${it.message.toString()}")
//        }
//
//        return Result.success()
//    }
}