package com.almikey.jiplace.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat

class CommonTest {
    val dateString = "11/03/2019 14:26";
    val df = SimpleDateFormat("dd/MM/yyyy HH:mm")

    @Test
    fun timeMinuteGroupUp() {
        val date = df.parse(dateString)
        assertEquals(1552303800000L, Common.timeMinuteGroupUp(date.time, 10))
        assertEquals(1552303800000L, Common.timeMinuteGroupUp(date.time, 15))
    }

    @Test
    fun timeMinuteGroupDown() {
        val date = df.parse(dateString)
        assertEquals(1552303200000L, Common.timeMinuteGroupDown(date.time, 10))
        assertEquals(1552302900000L, Common.timeMinuteGroupDown(date.time, 15))
    }
}