package com.almikey.jiplace

import android.util.Log
import androidx.test.runner.AndroidJUnit4
import com.almikey.jiplace.database.MyPlacesRoomDatabase
import com.almikey.jiplace.di.KoinModules.Companion.roomTestModule
import com.almikey.jiplace.model.MyLocation
import com.almikey.jiplace.model.MyPlace
import com.almikey.myplace.service.MyPlacesDao
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.test.KoinTest
import org.koin.test.inject
import java.util.*

@RunWith(AndroidJUnit4::class)
class MyPlacesDaoTest:KoinTest {
    val myPlacesDb:MyPlacesRoomDatabase by inject()
    val myPlacesDao:MyPlacesDao by inject()

    @Before()
    fun before(){
        loadKoinModules(roomTestModule)
    }

    @After
    fun after() {
        myPlacesDb.close()
    }

    @Test
    fun testSave(){
        val location = MyLocation(33.toFloat(),45.toFloat())
        val now = Date()

        var myPlace1  = MyPlace(location=location,time=now)

        val location2 = MyLocation(63.toFloat(),95.toFloat())
        val now2 = Date()

        var myPlace2  = MyPlace(location=location2,time=now2)

        val location3 = MyLocation(69.toFloat(),24.toFloat())
        val now3 = Date()

        var myPlace3  = MyPlace(location=location3,time=now3)

        myPlacesDao.insertAll(myPlace1,myPlace2,myPlace3)

        var places = myPlacesDao.getAll().blockingFirst()

        Log.d("my places",places.toString())
        Assert.assertThat(places, hasSize(3))
    }

}