package com.almikey.jiplace.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = arrayOf(
    Index(value = ["firebaseUid"],
        unique = true)
))
data class OtherUser(@PrimaryKey(autoGenerate = true) var otherId: Int=0, var firebaseUid:String)