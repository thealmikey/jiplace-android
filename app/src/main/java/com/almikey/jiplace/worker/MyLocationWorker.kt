package com.almikey.jiplace.worker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.almikey.jiplace.database.MyPlacesRoomDatabase
import com.almikey.jiplace.model.MyLocation
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.util.CurrentLocationRx
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*
import java.util.concurrent.TimeUnit


class MyLocationWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {

    val currLocRx: CurrentLocationRx by inject()
    val myPlacesDb = Room.databaseBuilder(applicationContext, MyPlacesRoomDatabase::class.java, "myplaces-db")
        .build()
    val myPlacesDao = myPlacesDb.myPlacesDao()

    val uuidKey = inputData.getString("UuidKey")
    var thePlace = myPlacesDao.findByUuid(uuidKey!!).blockingFirst()
    @SuppressLint("CheckResult")
    override fun doWork(): Result {
        try {
            runBlocking {
                //GPS sensors produces very many locations points per second. We take 4 of them and average them out to make
                // a location
                var locationList: List<Location> = currLocRx.observeLocation.subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(Schedulers.io())
                    .distinct().take(4).blockingIterable().toList()
                var firstLocation: MyLocation = MyLocation(0.0, 0.0)

                for (i in locationList) {
                    firstLocation.longitude += i.longitude.toFloat()
                    firstLocation.latitude += i.latitude.toFloat()
                }
                var location =
                    MyLocation(firstLocation.longitude / locationList.size, firstLocation.latitude / locationList.size)

                Log.d("got location ", " from work ${location.latitude}-${location.longitude}")
                var newLocation = MyLocation(location.latitude, location.longitude)
                var newPlace: MyPlace = thePlace.copy(location = newLocation, workSync = true)
                CompletableFromAction {
                    myPlacesDao.update(newPlace).subscribe()
                }.subscribeOn(Schedulers.io()).blockingAwait()
                Log.d("my place", " got the location for ${thePlace.hint}")

            }
            return Result.success()
        } catch (e: Exception) {
            Log.d("something wrong", "unable to get location, retrying")
            Result.retry()
        }
        return Result.failure()
    }
}