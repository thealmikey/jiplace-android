package com.almikey.jiplace.worker

import android.content.Context
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.almikey.jiplace.database.MyPlacesRoomDatabase
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.service.MyPlaceServerSyncService.MyPlaceServerSyncServiceImpl
import com.firebase.geofire.GeoFire
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject


class MyPlacesServerSyncWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters), KoinComponent {

    val myPlaceServerSyncServiceImpl:MyPlaceServerSyncServiceImpl by inject()

    val myPlacesDb = Room.databaseBuilder(applicationContext, MyPlacesRoomDatabase::class.java, "myplaces-db")
        .build()
    val myPlacesDao = myPlacesDb.myPlacesDao()
    //for every user in the db who's not synced into the server(firebase at the moment), we loop through and sync
    var thePlaces: List<MyPlace> = myPlacesDao.findByFbSyncStatus().blockingFirst()

    override fun doWork(): Result {
        try {
            runBlocking {
                myPlaceServerSyncServiceImpl.createMyPlacesOnServer(*thePlaces.toTypedArray())
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }


}