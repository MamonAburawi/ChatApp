package com.chatapp.info.data

import android.os.Parcelable
import androidx.room.Entity
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
@Entity(tableName = "chats")
class ChatDetails(
    val recipient: User? = null,
    val lastMessage: String = "",
    val lastTime: Date = Date(),
    val image: String = ""
): Parcelable {
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
           "recipientName" to recipient!!,
            "lastMessage" to lastMessage,
            "lastTime" to lastTime,
            "image" to image
        )
    }
}
