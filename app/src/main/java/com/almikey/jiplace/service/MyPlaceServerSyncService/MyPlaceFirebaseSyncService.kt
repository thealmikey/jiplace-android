package com.almikey.jiplace.service.MyPlaceServerSyncService

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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.HashMap

class MyPlaceFirebaseSyncService(val myPlacesRepositoryImpl: MyPlacesRepositoryImpl) : MyPlaceServerSyncService,
    KoinComponent {

    val myPlaceUserSharedDao: MyPlaceUserSharedDao by inject()

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
                                    Log.d(
                                        "periodic firebase",
                                        "firebasewas successful in periodically collecting places"
                                    )
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
                                    Log.d(
                                        "periodic firebase",
                                        "firebasewas successful in periodically collecting places"
                                    )
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

    override fun deleteMyPlacesOnServer(vararg myPlaces: MyPlace) {
//TODO implement delete
    }

    override fun deleteMyPlaceOnServer(myPlace: MyPlace) {
        var theFbId = FirebaseAuth.getInstance().uid!!
        var ref: DatabaseReference = FirebaseDatabase
            .getInstance().reference

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

        ref.updateChildren(childUpdates).addOnSuccessListener {
            CompletableFromAction {
                var newPlace = myPlace.copy(deletedStatus = "true")
                myPlacesRepositoryImpl.update(newPlace)
                deleteOtherUsersFromDeletedPlace(myPlace.uuidString, myPlaceUserSharedDao)
            }.subscribeOn(Schedulers.io()).subscribe {
                var myPlace = myPlacesRepositoryImpl.findByUuid(myPlace.uuidString).blockingFirst()
                myPlacesRepositoryImpl.delete(myPlace)
            }
        }
    }
}

fun deleteOtherUsersFromDeletedPlace(placeUUID: String, myPlaceUserSharedDao: MyPlaceUserSharedDao) {
    var usersInPlace: List<MyPlaceUserShared> = myPlaceUserSharedDao
        .findByMyPlaceUuid(placeUUID)
        .blockingFirst()
    Log.d("users in place", "${usersInPlace.size}")
    var myPlacesShared: List<MyPlaceUserShared> = myPlaceUserSharedDao
        .findByMyPlaceUuid(placeUUID)
        .blockingFirst()

    myPlaceUserSharedDao.delete(*myPlacesShared.toTypedArray())
    for (i in usersInPlace) {
        Log.d("shared user", "i got in the loop ${usersInPlace.size}")
        var userTimes: Int =
            myPlaceUserSharedDao.findByMyPlaceUuid(placeUUID).blockingFirst().size
        Log.d("shared user", "the number of jiplaces shared is $userTimes")
        if (userTimes == 0) {
            var wrapper: UserWrapper = UserWrapper.initWithEntityId(i.otherUserId);
            var userObservable = wrapper.metaOn();

            var otherUser =
                userObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                    .blockingFirst()
            if (otherUser.entityID!! != null) {
                for (thread in ChatSDK.thread().getThreads(ThreadType.Private1to1)) {
                    if (thread.getUsers().size === 2 &&
                        thread.containsUser(ChatSDK.currentUser()) &&
                        thread.containsUser(otherUser)
                    ) {
                        var jointThread = thread
                        DaoCore.deleteEntity(jointThread);
                        DaoCore.deleteEntity(otherUser);
                        var refUserChatLink: DatabaseReference =
                            FirebaseDatabase.getInstance()
                                .getReference("myplaceusers/chat/${FirebaseAuth.getInstance().uid!!}/${otherUser.entityID}")
                        var refUserChatOtherLink: DatabaseReference =
                            FirebaseDatabase.getInstance()
                                .getReference("myplaceusers/chat/${otherUser.entityID}/${FirebaseAuth.getInstance().uid!!}")
                        refUserChatLink.setValue(false)
                        refUserChatOtherLink.setValue(false)
                    }
                }
            }

        }
    }
}