package com.chatapp.info.data


data class MessageInfo(
    val index: Int,
    val message: Message,
    val isSending: Boolean? = null
)
