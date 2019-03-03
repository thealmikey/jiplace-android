package com.almikey.jiplace.util

object Common {
//functions to round time up and down, it takes time and time to round up
    //timeMinuteGroupUp(12:23,10) would produce 12:30, timeMinuteGroupDown(12:23,10) becomes 12:20
    //timeMinuteGroupUp(12:23,15) would produce 12:30,timeMinuteGroupDown(12:23,15) would produce 12:15
    //it helps store things in the database at a granularity of intervals of whatever we're rounding up and down
    //to
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

}