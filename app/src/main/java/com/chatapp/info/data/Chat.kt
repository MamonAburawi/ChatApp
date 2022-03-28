package com.chatapp.info.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.HashMap

@Parcelize
@Entity(tableName = "chat")
    data class Chat (
    @PrimaryKey
    val chatId: String = "",
    val senderId: String = "",
    val recipientId: String = "",
    var senderName: String = "",
    var recipientName: String = "",
    var lastMessage: String = "",
    var image: String = "",
    var lastUpdate: Date = Calendar.getInstance().time,

    ): Parcelable {
        fun toHashMap(): HashMap<String, Any> {
            return hashMapOf(
                "chatId" to chatId,
                "senderId" to senderId,
                "recipientId" to recipientId,
                "senderName" to senderName,
                "recipientName" to recipientName,
                "lastMessage" to lastMessage,
                "image" to image,
                "lastUpdate" to lastUpdate,
            )
        }
    }
