package com.chatapp.info.utils


import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.chatapp.info.data.User
import java.util.*

const val ERR_UPLOAD = "UploadErrorException"

const val KEY_RECIPIENT = "Recipient"
const val KEY_MESSAGE = "Message"

enum class StoreDataStatus { LOADING, ERROR, DONE }
enum class MessageType {TEXT , IMAGE, TEXT_IMAGE}
enum class ChatStatus {UPLOADING, UPDATING, OFFLINE}


class MyOnFocusChangeListener : View.OnFocusChangeListener {
    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null) {
            val inputManager =
                v.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (!hasFocus) {

                inputManager.hideSoftInputFromWindow(v.windowToken, 0)
            } else {
                inputManager.toggleSoftInputFromWindow(v.windowToken, 0, 0)

            }
        }
    }
}


internal fun getMessageId(userId: String, chatId: String): String {
    val uniqueId = UUID.randomUUID().toString()
    return "messageId-$chatId-$userId-$uniqueId"
}



internal fun getChatId(senderId: String, recipientId: String): String {
    return "chatId-$senderId-$recipientId"
}




fun <T,R> Collection<T>.findDiffElements(elements: Collection<T>,selector:(T)->R?) =
    filter{t -> elements.none{selector(it) == selector(t)}}



fun findCommon(first: List<User.Chat>, second: List<User.Chat>): String{
    var chatId = ""
    first.forEach { chat ->
        second.forEach { chat2 ->
            if (chat.chatId == chat2.chatId){
                chatId = chat.chatId
            }
        }
    }
  return chatId
}




fun genUUID() = UUID.randomUUID().toString()


