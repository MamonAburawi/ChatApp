package com.chatapp.info.data

import android.os.Parcelable
import androidx.room.*
import com.chatapp.info.utils.ObjectListDataTypeConverter
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.collections.ArrayList


// TODO: add the friends data inside the User data class.

@Entity(tableName = "user")
@Parcelize
data class User(
    @PrimaryKey
    var userId: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val date: Date = Date(),
    val age: Int = 0,
//    val friends: List<String> = ArrayList(),
    @TypeConverters(ObjectListDataTypeConverter::class)
    var chats: List<Chat> = ArrayList(),
    val lastOnline: Date = Calendar.getInstance().time
): Parcelable {
    fun toHashMap(): HashMap<String, Any> {
        return hashMapOf (
            "userId" to userId,
            "name" to name,
            "email" to email,
            "password" to password,
            "date" to date,
            "age" to age,
//            "friends" to friends,
            "chats" to chats,
            "lastOnline" to lastOnline
        )
    }


    @Parcelize
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

}