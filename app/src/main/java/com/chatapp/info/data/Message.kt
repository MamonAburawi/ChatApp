package com.chatapp.info.data

import androidx.room.*
import com.chatapp.info.MessageType
import java.util.*


//@Entity(
//    tableName = "messages",
//    foreignKeys = [ForeignKey(
//        entity = User::class,
//        parentColumns = ["user_id"],
//        childColumns = ["message_id"], // user
//        onDelete = ForeignKey.CASCADE
//    )]
//)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "message_id")
    val id: Long = 0L,
    @ColumnInfo(name = "text", defaultValue = "") var text: String = "",
    @ColumnInfo(name = "time") val time: Date = Calendar.getInstance().time,
    @ColumnInfo(name = "sender_id") val senderId: String = "",
    @ColumnInfo(name = "recipient_id") val recipientId: String = "",
    @ColumnInfo(name = "image") val imageId: String? = null,
    @ColumnInfo(name = "type") val type: String = "",
    @ColumnInfo(name = "chatId") val chatId: String? = null

)


