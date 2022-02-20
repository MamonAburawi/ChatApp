package com.chatapp.info


import android.annotation.SuppressLint
import android.content.Context
import java.util.*

@SuppressLint("StaticFieldLeak")
var utilContext :Context? = null

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


