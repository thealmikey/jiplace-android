package com.almikey.jiplace.service.MyPlaceServerSyncService

import android.util.Log
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.repository.MyPlacesRepositoryImpl
import com.almikey.jiplace.util.Common
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers

class MyPlaceFirebaseSyncService(val myPlacesRepositoryImpl: MyPlacesRepositoryImpl):MyPlaceServerSyncService {

    override fun createMyPlacesOnServer(vararg myPlace: MyPlace) {
        lateinit var theFbId: String
        var firebaseAuth = FirebaseAuth.getInstance()
        var authStateListener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            var theDeed = myPlace.forEach {
                //round up and round down time to ensure user can be found in as many groups as possible
//            var fiveMinGroupUp = timeMinuteGroupUp(it.time.time, 5).toString()
                var fifteenMinGroupUp = Common.timeMinuteGroupUp(it.time.time, 15).toString()
//            var fiveMinGroupDown = timeMinuteGroupDown(it.time.time, 5).toString()
                var fifteenMinGroupDown = Common.timeMinuteGroupDown(it.time.time, 15).toString()


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
                                        myPlacesRepositoryImpl.update(newPlace)
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
                                        myPlacesRepositoryImpl.update(newPlace)
                                    }.subscribeOn(Schedulers.io()).subscribe {
                                        Log.d("moved threads", "in order to update db")
                                    }
                                }
                            }
                        });
                }

            }
        }
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    override fun updateMyPlacesOnServer(vararg myPlace: MyPlace) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMyPlacesOnServer(vararg myPlace: MyPlace) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}