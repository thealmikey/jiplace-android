package com.almikey.jiplace.model

import android.location.Location
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Entity(indices = arrayOf(
    Index(value = ["uuidString"],
    unique = true)))
data class MyPlace(
    @PrimaryKey(autoGenerate = true) var jid: Int=0,
    var uuidString:String="",
    var time: Date=Date(),
    var timeRoundUp:Long= timeMinuteGroupUp(Date().time,15),
    var timeRoundDown:Long= timeMinuteGroupDown(Date().time, 15),
    @Embedded var location: MyLocation=MyLocation(0.0,0.0),
    var hint:String="",
    var image:String="",
    var people:Int=0,
    var newPeople:Int=0,
    var workSync:Boolean=false,
    var firebaseSync:Boolean=false,
    var deletedStatus:String="false",
    var jiplaceOther:Boolean=false,
    var dateAdded:Date=Date(),
    @Embedded var profile:MyPlaceProfilePic=MyPlaceProfilePic()
)

fun timeMinuteGroupUp(theTime: Long, min: Int): Long {
    var timeInSec = theTime.toFloat() / 1000
    var timeInMin = timeInSec / 60
    var timeIn15 = timeInMin / min
    var fixedTime = Math.floor(timeIn15.toDouble())
    var timeInMs = fixedTime * min * 60 * 1000
    return timeInMs.toLong()
}
fun timeMinuteGroupDown(theTime: Long, min: Int): Long {
    var timeInSec = theTime.toFloat() / 1000
    var timeInMin = timeInSec / 60
    var timeIn15 = timeInMin / min
    var fixedTime = Math.ceil(timeIn15.toDouble())
    var timeInMs = fixedTime * min * 60 * 1000
    return timeInMs.toLong()
}