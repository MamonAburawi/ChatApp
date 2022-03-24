//package com.chatapp.info.screens.chat
//
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import androidx.lifecycle.viewModelScope
//import com.chatapp.info.ServiceLocator.messageRepository
//import com.chatapp.info.data.Message
//import com.chatapp.info.utils.findDiffElements
//import kotlinx.coroutines.DelicateCoroutinesApi
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.GlobalScope
//import kotlinx.coroutines.launch
//
//
//@DelicateCoroutinesApi
//class ChatReceiver(val chatId: String) : BroadcastReceiver(){
//    companion object{
//        const val TAG = "ChatReceiver"
//    }
//    private var newMessages = emptyList<Message>()
//    override fun onReceive(context: Context?, intent: Intent?) {
//
//
//
//    }
//
//
//
//    interface ChatListener{
//        fun onNewMessage(chatId: String): List<Message> {
//            GlobalScope.launch(Dispatchers.IO) {
//                messageRepository?.observeMessagesOnRemoteByChatId(chatId){ messages ->
//
//                }
//            }
//        }
//    }
//
//
//    fun x(){
//
//
//    }
//
//    private fun refreshMessages(remoteMessages: List<Message>, localMessages: List<Message>): List<Message> {
//        GlobalScope.launch(Dispatchers.IO) {
//            Log.d(TAG,"remote Messages: ${remoteMessages.size}")
//            Log.d(TAG,"local Messages: ${localMessages.size}")
//            newMessages = remoteMessages.findDiffElements(localMessages){it.messageId}
//            Log.d(TAG,"new Messages: ${newMessages.size}")
//            messageRepository?.insertMultipleMessages(newMessages)
//        }
//     return   newMessages
//    }
//
//}