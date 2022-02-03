package com.chatapp.info.data

import java.util.*

data class Chat(
    val chatId: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    val createDate: Date = Calendar.getInstance().time
)
