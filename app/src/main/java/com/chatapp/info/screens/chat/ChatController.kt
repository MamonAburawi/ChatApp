package com.chatapp.info.screens.chat

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.airbnb.epoxy.TypedEpoxyController
import com.chatapp.info.*
import com.chatapp.info.data.Message
import com.chatapp.info.utils.ChatAppSessionManager
import com.chatapp.info.utils.MessageType

class ChatController(val context: Context,val onClickListener: MessageClickListener) : TypedEpoxyController<List<Message>>() {

    companion object{
        private const val TAG = "chatFragment"
    }

    private val sessionManager by lazy { ChatAppSessionManager(context) }
    private val userId = sessionManager.getUserIdFromSession()


    override fun buildModels(data: List<Message>?) {
        data?.forEachIndexed { index, message ->

            if (message.senderId == userId){ // sender
                senderViews(message)

            }else{ // recipient

                recipientViews(message)

            }

        }
    }


    private fun senderViews(message: Message){

        when(message.type){

            MessageType.TEXT.name->{ // message type text
                Log.d(TAG,"sender: message type text: ${message.text} ")
                senderMessage {
                    id(message.messageId)
                    message(message)
                    clickListener { v ->
                        onClickListener.onClick(message)
                    }
                }
            }

            // TODO: display layout that have multiple images
            MessageType.IMAGE.name->{ // message type image

                val imagesUri = message.images.map { it.toUri() } as MutableList<Uri>

                val imgUrl = imagesUri[0]
                Log.d(TAG,"sender: message type image: $imagesUri ")
                senderImage {
                    id(message.messageId)
                    message(message)
                    uri(imgUrl)
                    clickListener { v ->

                    }
                }



            }
            MessageType.TEXT_IMAGE.name ->{ // message type text & image
                Log.d(TAG,"sender: message type text & image ")
            }
        }
    }


    private fun recipientViews(message: Message){
        when(message.type){
            MessageType.TEXT.name->{ // message type text
                Log.d(TAG,"recipient: message type text: ${message.text} ")
                recipientMessage {
                    id(message.messageId)
                    message(message)
                    clickListener { v ->
                        onClickListener.onClick(message)
                    }
                }

            }
            MessageType.IMAGE.name->{ // message type image
                        val imagesUri = message.images.map { it.toUri() } as MutableList<Uri>
                        val imgUrl = imagesUri[0]
                        recepientImage {
                            id(message.messageId)
                            message(message)
                            uri(imgUrl)
                            clickListener { v ->

                            }
                        }

            }
            MessageType.TEXT_IMAGE.name ->{ // message type text & image

            }
        }

    }



    class MessageClickListener(val clickListener: (message: Message) -> Unit) {
        fun onClick(message: Message) = clickListener(message)
    }

}
