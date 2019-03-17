package com.almikey.jiplace.model

import com.almikey.jiplace.util.Common.generateRandomUUID
import org.junit.Test

import org.junit.Assert.*
import java.lang.IllegalArgumentException
import org.joda.time.DateTime
import java.util.*


class MyPlaceTest {


    @Test(expected = IllegalArgumentException::class)
    fun `createMyPlaceWithBlankUUIDString_shouldThrow_IllegalArgumentException`(){
        var myPlace = MyPlace(uuidString = "")
    }
    @Test(expected = IllegalArgumentException::class)
    fun `createMyPlaceInTheFuture_shouldThrow_IllegalArgumentException`(){
        var date = Date()
        var dateTime = DateTime(date)
        var futureDate = dateTime.plusDays(1).toDate()
        var myPlace = MyPlace(uuidString= generateRandomUUID(),jiplaceOther = true,time = futureDate)
    }
}