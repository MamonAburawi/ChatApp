package com.chatapp.info.screens.chats

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.airbnb.epoxy.TypedEpoxyController
import com.chatapp.info.*
import com.chatapp.info.data.ChatDetails
import com.chatapp.info.data.Message
import com.chatapp.info.data.User
import com.chatapp.info.utils.ChatAppSessionManager
import com.chatapp.info.utils.MessageType

class ChatsController(val context: Context, val onClickListener: ChatClickListener) : TypedEpoxyController<List<ChatDetails>>() {

    companion object{
        private const val TAG = "ChatsController"
    }

    private val sessionManager by lazy { ChatAppSessionManager(context) }
    private val userId = sessionManager.getUserIdFromSession()


    override fun buildModels(data: List<ChatDetails>?) {
        data?.forEachIndexed { index, chatDetails ->

            chat {
                id(index)
                chat(chatDetails)
                clickListener { v->
                    onClickListener.onClick(chatDetails)
                }
            }
        }
    }



    class ChatClickListener(val clickListener: (ChatDetails) -> Unit) {
        fun onClick(chat: ChatDetails) = clickListener(chat)
    }

}

