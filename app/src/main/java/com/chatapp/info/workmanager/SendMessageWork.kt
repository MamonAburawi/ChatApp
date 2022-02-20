package com.chatapp.info.workmanager

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chatapp.info.data.DateConverter
import com.chatapp.info.data.Message
import com.chatapp.info.screens.chat.ChatViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import java.util.*

class SendMessageWork(context : Context, paramsWorker: WorkerParameters) : CoroutineWorker(context,paramsWorker) {

    // firebase fire store
    private val _root = FirebaseFirestore.getInstance()
    private val chatsPath = _root.collection("chats")

    override suspend fun doWork(): Result {

//        val currentTime = Calendar.getInstance().time
        val date = inputData.getLong("date",0L)
        val longToDate = DateConverter.from(date)
        val chatId = inputData.getString("chat_id")

        val message = Message(
            inputData.getLong("message_id",0L),
            inputData.getString("text")!!,
            longToDate!!,
            inputData.getString("sender_id")!!,
            inputData.getString("recipient_id")!!,
            inputData.getString("image")!!,
            inputData.getString("type")!!
        )

        try {
            chatsPath.document(chatId!!).collection("messages").document(message.id.toString())
                .set(message)
                .addOnCompleteListener {

                    Result.success()

                }.addOnFailureListener {}

        }catch(ex:Exception){
            Log.i(ChatViewModel.TAG,ex.message.toString())
        }



        return Result.success()
    }

}