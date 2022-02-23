package com.chatapp.info.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chatapp.info.screens.chat.ChatViewModel
import com.google.firebase.firestore.FirebaseFirestore


class RemoveMessageWork(context : Context, paramsWorker: WorkerParameters) : CoroutineWorker(context,paramsWorker) {

    // firebase fire store
    private val _root = FirebaseFirestore.getInstance()
    private val chatsPath = _root.collection("chats")

    private val chatId = inputData.getString("chat_id")!!
    private val messageId = inputData.getString("message_id")

    override suspend fun doWork(): Result {

        try {
            chatsPath.document(chatId)
                .collection("messages")
                .document(messageId!!)
                .delete()
                .addOnSuccessListener {
                    chatsPath.document().collection("messages").document(messageId).get()
                        .addOnSuccessListener {
                            if (it.exists()){
                                Log.i(ChatViewModel.TAG,"message is not removed!")
                                Result.failure()
                            }else{
                                Log.i(ChatViewModel.TAG,"message is removed")
                                Result.success()
                            }
                        }
                }
                .addOnFailureListener {
                    Log.i(ChatViewModel.TAG,it.message.toString())
                     Result.failure()
                }
        }catch (ex: Exception){
            Log.i(ChatViewModel.TAG,ex.message.toString())
            return Result.failure()
        }

        return Result.success()
    }
}