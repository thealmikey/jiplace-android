package com.almikey.jiplace.model

import android.location.Location
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class MyPlace(
    @PrimaryKey(autoGenerate = true) var jid: Int=0,
    var uuidString:String="",
    var time: Date=Date(),
    @Embedded var location: MyLocation=MyLocation(0f,0f),
    var hint:String="",
    var image:String="",
    var people:Int=0,
    var newPeople:Int=0,
    var workSync:Boolean=false,
    var firebaseSync:Boolean=false,
    var deletedStatus:String="false",
    var jiplaceOther:Boolean=false,
    var dateAdded:Date=Date()
)