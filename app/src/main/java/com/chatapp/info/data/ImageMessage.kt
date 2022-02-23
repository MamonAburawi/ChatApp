package com.chatapp.info.data

import android.net.Uri

data class ImageMessage(
    val message: Message,
    val image: Uri? = null
)
