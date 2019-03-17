package com.almikey.jiplace.util

import java.util.*

object Common {
    // functions to round time up and down, it takes time and time to round up
    // timeMinuteGroupUp(12:23,10) would produce 12:30, timeMinuteGroupDown(12:23,10) becomes 12:20
    // timeMinuteGroupUp(12:23,15) would produce 12:30,timeMinuteGroupDown(12:23,15) would produce 12:15
    // it helps store things in the database at a granularity of intervals of whatever we're rounding up and down
    // to
    fun timeMinuteGroupUp(theTime: Long, min: Int): Long {
        val timeInSec = theTime.toFloat() / 1000
        val timeInMin = timeInSec / 60
        val timeIn15 = timeInMin / min
        val fixedTime = Math.ceil(timeIn15.toDouble())
        val timeInMs = fixedTime * min * 60 * 1000
        return timeInMs.toLong()
    }

    fun timeMinuteGroupDown(theTime: Long, min: Int): Long {
        val timeInSec = theTime.toFloat() / 1000
        val timeInMin = timeInSec / 60
        val timeIn15 = timeInMin / min
        val fixedTime = Math.floor(timeIn15.toDouble())
        val timeInMs = fixedTime * min * 60 * 1000
        return timeInMs.toLong()
    }

    fun generateRandomUUID(): String {
        return UUID.randomUUID().toString()
    }

}