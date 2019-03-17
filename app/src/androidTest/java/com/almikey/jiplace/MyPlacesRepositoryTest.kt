package com.almikey.jiplace

import android.util.Log
import androidx.test.runner.AndroidJUnit4
import com.almikey.jiplace.database.MyPlacesRoomDatabase
import com.almikey.jiplace.di.KoinModules
import com.almikey.jiplace.di.KoinModules.Companion.roomTestModule
import com.almikey.jiplace.model.MyLocation
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.repository.MyPlacesRepositoryImpl
import com.almikey.myplace.service.MyPlacesDao
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.standalone.StandAloneContext.closeKoin
import org.koin.standalone.StandAloneContext.loadKoinModules
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.standalone.inject
import org.koin.test.KoinTest
import java.util.*

@RunWith(AndroidJUnit4::class)
class MyPlacesRepositoryTest :KoinTest{

    val myPlacesRoomDatabase:MyPlacesRoomDatabase by inject(name = "test_roomdb")
    val myPlacesRepositoryImpl:MyPlacesRepositoryImpl by inject(name = "test_myPlaceRepositoryImpl")

    @Before()
    fun before(){
       loadKoinModules(KoinModules.roomTestModule)
    }

    @After
    fun after() {
        stopKoin()
    }

    @Test
    fun testSave(){
        val location = MyLocation(33.0,45.0)
        val now = Date()
        var myPlace1  = MyPlace(location=location,time=now)

        myPlacesRepositoryImpl.addMyPlace(myPlace1).blockingGet()

        var places =  myPlacesRepositoryImpl.findAll().blockingFirst()
        Log.d("my places",places.toString())
        Assert.assertThat(places, hasSize(1))
    }

}