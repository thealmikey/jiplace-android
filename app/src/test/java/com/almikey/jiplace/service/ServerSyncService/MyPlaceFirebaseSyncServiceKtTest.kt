package com.almikey.jiplace.service.ServerSyncService

import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.util.Common.generateRandomUUID
import org.junit.Test

import org.junit.Assert.*

class MyPlaceFirebaseSyncServiceKtTest {

    @Test
    fun `createMyPlaceHashMap_should_createAHashMap from firebase uid and MyPlace`(){
        var firebaseId = "123"
        var roundedJiplaceKey = "123"
        var timeGroup = "123"

        var myPlace = MyPlace(uuidString = generateRandomUUID(),  hint = "hint")

        var placeHashMap = hashMapOf<Any, Any>(
            "jiplaces/jiplacesKeyValue/$roundedJiplaceKey" to "123",
            "myplaceusers/$firebaseId/$timeGroup" to "123",
            "myplaceusers/$firebaseId/$roundedJiplaceKey/hint" to "hint",
            "myplaceusers/$firebaseId/$roundedJiplaceKey/dateAdded" to myPlace.dateAdded.toString()
        )
        assertEquals(createMyPlaceHashMap("123","123","123",myPlace),placeHashMap)
}
}