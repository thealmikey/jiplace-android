package com.almikey.jiplace.model

import android.location.Location
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.joda.time.DateTime

@Entity
data class MyPlace(
    @PrimaryKey(autoGenerate = true) var jid: Int,
    var time:DateTime, var location: Location, var hint:String="",
    var image:String="", var people:Int=0, var newPeople:Int=0)