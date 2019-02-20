package com.almikey.jiplace.worker

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.almikey.jiplace.database.MyPlacesRoomDatabase
import com.almikey.jiplace.model.MyPlace
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking


class MyPlacesFirebaseSyncWorker(context: Context, workerParameters: WorkerParameters) :
    Worker(context, workerParameters) {


    val myPlacesDb = Room.databaseBuilder(applicationContext, MyPlacesRoomDatabase::class.java, "myplaces-db")
        .build()
    val myPlacesDao = myPlacesDb.myPlacesDao()
    lateinit var theFbId: String
    var thePlaces: List<MyPlace> = myPlacesDao.findByFbSyncStatus().blockingFirst()
    //for every user in the db who's not synced into firebase, we
    var firebaseAuth = FirebaseAuth.getInstance()
    var authStateListener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        var theDeed = thePlaces.forEach {
            //round up and round down time to ensure user can be found in as many groups as possible
//            var fiveMinGroupUp = timeMinuteGroupUp(it.time.time, 5).toString()
            var fifteenMinGroupUp = timeMinuteGroupUp(it.time.time, 15).toString()
//            var fiveMinGroupDown = timeMinuteGroupDown(it.time.time, 5).toString()
            var fifteenMinGroupDown = timeMinuteGroupDown(it.time.time, 15).toString()


            var ref: DatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference("jiplaces/fifteen/$fifteenMinGroupUp");
            var geoFire: GeoFire = GeoFire(ref);
            var refDown: DatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference("jiplaces/fifteen/$fifteenMinGroupDown");
            var geoFireDown: GeoFire = GeoFire(refDown);
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                var theFbId = firebaseUser.uid
                geoFire.setLocation(
                    "$theFbId",
                    GeoLocation(it.location.latitude.toDouble(), it.location.longitude.toDouble()),
                    object : GeoFire.CompletionListener {
                        override fun onComplete(key: String?, error: DatabaseError?) {
                            if (error != null) {
                                Log.d("periodic firebase", "firebase unable to periodically collect places")
                            } else {
                                Log.d("periodic firebase", "firebasewas successful in periodically collecting places")
                                var newPlace: MyPlace = it.copy(firebaseSync = true)
                                CompletableFromAction {
                                    myPlacesDao.update(newPlace)
                                }.subscribeOn(Schedulers.io()).subscribe {
                                    Log.d("moved threads", "in order to update db")
                                }
                            }
                        }
                    });
                geoFireDown.setLocation(
                    "$theFbId",
                    GeoLocation(it.location.latitude.toDouble(), it.location.longitude.toDouble()),
                    object : GeoFire.CompletionListener {
                        override fun onComplete(key: String?, error: DatabaseError?) {
                            if (error != null) {
                                Log.d("periodic firebase", "firebase unable to periodically collect places")
                            } else {
                                Log.d("periodic firebase", "firebasewas successful in periodically collecting places")
                                var newPlace: MyPlace = it.copy(firebaseSync = true)
                                CompletableFromAction {
                                    myPlacesDao.update(newPlace)
                                }.subscribeOn(Schedulers.io()).subscribe {
                                    Log.d("moved threads", "in order to update db")
                                }
                            }
                        }
                    });
            }

        }
    }

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


    var ref = FirebaseDatabase.getInstance().getReference()
    var geoFire = GeoFire(ref)

    override fun doWork(): Result {
        try {
            runBlocking {
                firebaseAuth.addAuthStateListener(authStateListener)
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }


}