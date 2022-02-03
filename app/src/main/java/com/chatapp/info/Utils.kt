package com.chatapp.info


import java.util.*

object MessageType{
    const val TEXT = "Text"
    const val IMAGE = "Image"
    const val TEXT_IMAGE = "Text_Image"
}

object MessageState{
    const val INSERT = "Insert"
    const val DELETE = "Delete"
    const val UPDATE = "Update"
}


fun genUUID() = UUID.randomUUID().toString()


