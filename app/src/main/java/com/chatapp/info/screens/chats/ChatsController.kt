package com.chatapp.info.screens.chats

import android.content.Context
import com.airbnb.epoxy.TypedEpoxyController
import com.chatapp.info.*
import com.chatapp.info.data.Chat
import com.chatapp.info.data.User
import com.chatapp.info.utils.ChatAppSessionManager

class ChatsController(val context: Context, val onClickListener: ChatClickListener) : TypedEpoxyController<List<Chat>>() {

    companion object{
        private const val TAG = "ChatsController"
    }

    private val sessionManager by lazy { ChatAppSessionManager(context) }
    private val userId = sessionManager.getUserIdFromSession()


    override fun buildModels(data: List<Chat>?) {
        data?.forEachIndexed { index, chat ->

            chat {
                id(index)
                chat(chat)
                clickListener { v->
                    onClickListener.onClick(chat)
                }
            }
        }
    }



    class ChatClickListener(val clickListener: (Chat) -> Unit) {
        fun onClick(chat: Chat) = clickListener(chat)
    }

}

