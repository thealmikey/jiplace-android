package com.almikey.jiplace.service.ServerSyncService

import android.util.Log
import co.chatsdk.core.dao.DaoCore
import co.chatsdk.core.interfaces.ThreadType
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.firebase.wrappers.UserWrapper
import com.almikey.jiplace.database.dao.MyPlaceUserSharedDao
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.model.MyPlaceUserShared
import com.almikey.jiplace.repository.MyPlacesRepositoryImpl
import com.almikey.jiplace.util.Common
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.HashMap
import com.google.firebase.database.DataSnapshot



class MyPlaceFirebaseSyncService(val myPlacesRepositoryImpl: MyPlacesRepositoryImpl) : MyPlaceServerSyncService,
    KoinComponent {

    var theFbId = FirebaseAuth.getInstance().uid!!
    var ref: DatabaseReference = FirebaseDatabase
        .getInstance().reference

    override fun createMyPlacesOnServer(vararg myPlace: MyPlace): Task<Boolean> {
        val dbSource = TaskCompletionSource<Boolean>()
        val dbTask:Task<Boolean> = dbSource.getTask();
            myPlace.forEach {
                //round up and round down time to ensure user can be found in as many groups as possible
//            var fiveMinGroupUp = timeMinuteGroupUp(it.time.time, 5).toString()
                var fifteenMinGroupUp = Common.timeMinuteGroupUp(it.time.time, 15).toString()
//            var fiveMinGroupDown = timeMinuteGroupDown(it.time.time, 5).toString()
                var fifteenMinGroupDown = Common.timeMinuteGroupDown(it.time.time, 15).toString()


                var ref: DatabaseReference = FirebaseDatabase
                    .getInstance()
                    .getReference("jiplaces/fifteen/$fifteenMinGroupUp");
                var geoFireTimeRoundedUp: GeoFire = GeoFire(ref);
                var refDown: DatabaseReference = FirebaseDatabase
                    .getInstance()
                    .getReference("jiplaces/fifteen/$fifteenMinGroupDown");

                var geoFireTimeRoundedDown: GeoFire = GeoFire(refDown);
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    var theFbId = firebaseUser.uid
                    geoFireTimeRoundedUp.setLocation(
                        "$theFbId",
                        GeoLocation(it.location.latitude.toDouble(), it.location.longitude.toDouble()),
                        object : GeoFire.CompletionListener {
                            override fun onComplete(key: String?, error: DatabaseError?) {
                                if (error != null) {
                                    dbSource.setResult(true);
                                    Log.d("periodic firebase", "firebase unable to periodically collect places")
                                } else {
                                    dbSource.setResult(false)
                                    Log.d(
                                        "periodic firebase",
                                        "firebasewas successful in periodically collecting places"
                                    )
                                    var newPlace: MyPlace = it.copy(firebaseSync = true)
                                    CompletableFromAction {
                                        myPlacesRepositoryImpl.update(newPlace).subscribe()
                                    }.subscribeOn(Schedulers.io()).subscribe {
                                        Log.d("moved threads", "in order to update db")
                                    }
                                }
                            }
                        });
                    geoFireTimeRoundedDown.setLocation(
                        "$theFbId",
                        GeoLocation(it.location.latitude.toDouble(), it.location.longitude.toDouble()),
                        object : GeoFire.CompletionListener {
                            override fun onComplete(key: String?, error: DatabaseError?) {
                                if (error != null) {

                                    Log.d("periodic firebase", "firebase unable to periodically collect places")
                                } else {
                                    Log.d(
                                        "periodic firebase",
                                        "firebasewas successful in periodically collecting places"
                                    )
                                    var newPlace: MyPlace = it.copy(firebaseSync = true)
                                    CompletableFromAction {
                                        myPlacesRepositoryImpl.update(newPlace).subscribe()
                                    }.subscribeOn(Schedulers.io()).subscribe {
                                        Log.d("moved threads", "in order to update db")
                                    }
                                }
                            }
                        });

                }

            }

        return dbTask
    }

    override fun deleteMyPlaceOnServer(myPlace: MyPlace): Task<Void> {


        var fifteenMinGroupUp = Common.timeMinuteGroupUp(myPlace.time.time, 15).toString()
        var fifteenMinGroupDown = Common.timeMinuteGroupDown(myPlace.time.time, 15).toString()

        /**
         *    Jiplace are stored in 15 minute intervals
         *    if a person jiplaces themselves at 12:12, it's rounded up to 12:15 and down to 12:00
         *    if another person jiplaces themseleves at 11:40,it's rounded up to 12:00 and down to 11:30
         *    searches for jiplaces are then possible for people who fall in their rounded up and down
         *    categories
         *    this helps prevent searching the whole database for people who have placed themselves as
         *    too granular time would lead to too many queries especially as the database grows
         */
        val childUpdates = HashMap<String, Any?>()
        childUpdates["jiplaces/fifteen/$fifteenMinGroupUp/$theFbId"] = null
        childUpdates["jiplaces/fifteen/$fifteenMinGroupDown/$theFbId"] = null
        return ref.updateChildren(childUpdates)
    }
}