package com.chatapp.info.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

import com.chatapp.info.data.Message
import com.chatapp.info.screens.chat.ChatViewModel
import com.google.firebase.firestore.FirebaseFirestore


class SendMessageWork(context : Context, paramsWorker: WorkerParameters) : CoroutineWorker(context,paramsWorker) {
    override suspend fun doWork(): Result {
        TODO("Not yet implemented")
    }

//    // firebase fire store
//    private val _root = FirebaseFirestore.getInstance()
//    private val chatsPath = _root.collection("chats")
//
//    override suspend fun doWork(): Result {
//
//        val date = inputData.getLong("date",0L)
//        val longToDate = DateConverter.from(date)
//        val chatId = inputData.getString("chat_id")
//
//        val message = Message(
//            inputData.getLong("message_id",0L),
//            inputData.getString("text")!!,
//            longToDate!!,
//            inputData.getString("sender_id")!!,
//            inputData.getString("recipient_id")!!,
//            inputData.getString("image_id")!!,
//            inputData.getString("type")!!,
//            chatId
//        )
//
//        try {
//            chatsPath.document(chatId!!).collection("messages").document(message.messageId.toString())
//                .set(message)
//                .addOnCompleteListener {
//                    Result.success()
//                }
//                .addOnFailureListener {
//                    Result.failure()
//                }
//        }catch(ex:Exception){
//            Log.i(ChatViewModel.TAG,ex.message.toString())
//            Result.failure()
//        }
//
//
//
//        return Result.success()
//    }

}