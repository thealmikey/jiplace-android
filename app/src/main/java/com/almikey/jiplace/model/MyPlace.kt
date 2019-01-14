package com.almikey.jiplace.model

import android.location.Location
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class MyPlace(
    @PrimaryKey(autoGenerate = true) var jid: Int,
    var time: Date, @Embedded var location: MyLocation, var hint:String="",
    var image:String="", var people:Int=0, var newPeople:Int=0)