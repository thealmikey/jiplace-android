package com.almikey.jiplace.util


import com.pholser.junit.quickcheck.Property
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import org.hamcrest.Matchers.*
import org.junit.runner.RunWith


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

    @Test
    fun generateRandomUUID(){
        var uuid =  UUID.randomUUID().toString()
        assertNotNull(uuid)
        assertThat("random uuid size greater than 10",uuid.length, equalTo(32))
    }


}