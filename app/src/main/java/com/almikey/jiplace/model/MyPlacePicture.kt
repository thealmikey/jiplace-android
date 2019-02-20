package com.almikey.jiplace.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = arrayOf(
        Index(
            value = ["picId"],
            unique = true
        )
    ), foreignKeys = arrayOf(
        ForeignKey(
            entity = MyPlace::class,
            parentColumns = arrayOf("uuidString"),
            childColumns = arrayOf("placeUUID"),
            onDelete = ForeignKey.CASCADE
        )
    )

)

class MyPlacePicture(
    @PrimaryKey(autoGenerate = true) var picId: Int = 0,
    var firebasePicUrl: String,
    var localPicUrl:String,
    var placeUUID: String
)