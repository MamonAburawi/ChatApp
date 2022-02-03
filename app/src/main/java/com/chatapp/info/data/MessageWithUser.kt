package com.chatapp.info.data

import androidx.room.Embedded

data class MessageWithUser(
    @Embedded val message: Message,
    @Embedded val user: User
    )
