package com.almikey.jiplace.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = arrayOf(
    ForeignKey(entity = MyPlace::class,
    parentColumns = arrayOf("uuidString"),
    childColumns = arrayOf("sharedJiplaces"),
    onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = OtherUser::class,
        parentColumns = arrayOf("firebaseUid"),
        childColumns = arrayOf("otherUserId"),
        onDelete = ForeignKey.CASCADE)
))
data class MyPlaceUserShared(
    @PrimaryKey(autoGenerate = true) var myPlaceSharedId: Int=0,
    var otherUserId:String,
    var sharedJiplaces:String
)