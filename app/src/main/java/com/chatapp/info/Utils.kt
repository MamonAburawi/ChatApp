package com.chatapp.info


import java.util.*

object MessageType{
    const val TEXT = "Text"
    const val IMAGE = "Image"
    const val TEXT_IMAGE = "Text_Image"
}


fun genUUID() = UUID.randomUUID().toString()


