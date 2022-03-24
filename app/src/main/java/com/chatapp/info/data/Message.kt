package com.chatapp.info.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chatapp.info.utils.MessageType
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList


@Parcelize
@Entity(tableName = "messages")
data class Message (
    @PrimaryKey
    var messageId: String = "",
    var text: String = "",
    var date: Date = Date(),
    val senderId: String = "",
    val recipientId: String = "",
    var images: List<String> = ArrayList(),
    val chatId: String = "",
    val type: String = ""
):Parcelable{
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "messageId" to messageId,
            "text" to text,
            "date" to date,
            "senderId" to senderId,
            "recipientId" to recipientId,
            "images" to images,
            "chatId" to chatId,
            "type" to type
        )
    }
}