package com.chatapp.info.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*


@Entity(tableName = "users")
@Parcelize
data class User(
    @PrimaryKey
    @ColumnInfo(name = "user_id") var id: String = "",
    @ColumnInfo(name = "first_name") val name: String = "",
    @ColumnInfo(name = "email") val email: String = "",
    @ColumnInfo(name = "password") val password: String = "",
    @ColumnInfo(name = "createDate") val createDate: Date = Calendar.getInstance().time,
    @ColumnInfo(name = "age") val age: Int = 0,
    @ColumnInfo(name = "last_online") val lastOnline: Date = Calendar.getInstance().time
): Parcelable