package com.almikey.jiplace.service.ServerSyncService

import android.util.Log
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.repository.MyPlacesRepositoryImpl
import com.almikey.jiplace.util.Common
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.KoinComponent
import java.util.*


class MyPlaceFirebaseSyncService(val myPlacesRepositoryImpl: MyPlacesRepositoryImpl) : MyPlaceServerSyncService,
    KoinComponent {

    var theFbId = FirebaseAuth.getInstance().uid!!
    var ref: DatabaseReference = FirebaseDatabase
        .getInstance().reference

    override fun createMyPlacesOnServer(vararg myPlace: MyPlace): Task<Boolean> {

        lateinit var roundedUpJiplaceKey: String
        lateinit var roundedDownJiplaceKey: String

        val taskValueUp = TaskCompletionSource<Boolean>()

        //Task<True> means the JiPlaces was successfully saved on the server
        val taskUp: Task<Boolean> = taskValueUp.getTask();
        val taskValueDown = TaskCompletionSource<Boolean>()
        //Task<True> means the JiPlaces was successfully saved on the server
        val taskDown: Task<Boolean> = taskValueDown.getTask();
        myPlace.forEach {
            //round up and round down time to ensure user can be found in as many groups as possible
//            var fiveMinGroupUp = timeMinuteGroupUp(it.time.time, 5).toString()
            var fifteenMinGroupUp = Common.timeMinuteGroupUp(it.time.time, 15).toString()
//            var fiveMinGroupDown = timeMinuteGroupDown(it.time.time, 5).toString()
            var fifteenMinGroupDown = Common.timeMinuteGroupDown(it.time.time, 15).toString()


            var dbRef: DatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference();
            var jiPlacesKeyValue: DatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference("jiplaces/jiplacesKeyValue");

            var refUp: DatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference("jiplaces/fifteen/$fifteenMinGroupUp");
            var refDown: DatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference("jiplaces/fifteen/$fifteenMinGroupDown");


            roundedUpJiplaceKey = generateFirebaseItemKey("jiplaces/fifteen/$fifteenMinGroupUp")!!
            roundedDownJiplaceKey = generateFirebaseItemKey("jiplaces/fifteen/$fifteenMinGroupDown")!!

            Log.d("roundUpJiplaceKey", roundedUpJiplaceKey)
            Log.d("roundDownJiplaceKey", roundedDownJiplaceKey)

            if (roundedDownJiplaceKey != null && roundedUpJiplaceKey != null) {
                var geoFireTimeRoundedUp: GeoFire = GeoFire(refUp);
                var geoFireTimeRoundedDown: GeoFire = GeoFire(refDown);
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    var theFbId = firebaseUser.uid
                    geoFireTimeRoundedUp.setLocation(
                        roundedUpJiplaceKey,
                        GeoLocation(it.location.latitude, it.location.longitude),
                        object : GeoFire.CompletionListener {
                            override fun onComplete(key: String?, error: DatabaseError?) {
                                if (error == null) {


                                    val childUpdates =
                                        createMyPlaceHashMap(theFbId, roundedUpJiplaceKey, fifteenMinGroupUp, it)

                                    dbRef.updateChildren(childUpdates).addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            Log.d("serversync", "managed to create in firebase")
                                        } else {
                                            it.addOnFailureListener {
                                                Log.d("serversync", "the exception is ${it.message}")
                                            }
                                        }
                                    }
                                    Log.d("periodic firebase", "firebase unable to periodically collect places")

                                    var newPlace: MyPlace = it.copy(firebaseSync = true)
                                    myPlacesRepositoryImpl.update(newPlace).subscribeOn(Schedulers.io()).takeIf {
                                        !taskUp.isComplete
                                    }!!.doAfterSuccess {
                                        taskValueUp.setResult(true);
                                    }.doOnError {
                                        taskValueUp.setResult(false)
                                    }
                                        .blockingGet()
                                } else {

                                    Log.d(
                                        "periodic firebase",
                                        "firebase was unsuccessful in periodically collecting places"
                                    )

                                }
                            }
                        });
                    geoFireTimeRoundedDown.setLocation(
                        roundedDownJiplaceKey,
                        GeoLocation(it.location.latitude, it.location.longitude),
                        object : GeoFire.CompletionListener {
                            override fun onComplete(key: String?, error: DatabaseError?) {
                                if (error == null) {
                                    val childUpdates = createMyPlaceHashMap(
                                        theFbId,
                                        roundedDownJiplaceKey,
                                        fifteenMinGroupDown,
                                        it
                                    )
                                    Log.d("periodic firebase", "firebase able to periodically collect places")
                                    dbRef.updateChildren(childUpdates)
                                    var newPlace: MyPlace = it.copy(firebaseSync = true)
                                    myPlacesRepositoryImpl.update(newPlace).subscribeOn(Schedulers.io())
                                        .takeIf {
                                            !taskDown.isComplete
                                        }!!.doAfterSuccess {
                                        taskValueDown.setResult(true);
                                    }.doOnError {
                                        taskValueDown.setResult(false)
                                    }.blockingGet()
                                } else {
                                    Log.d(
                                        "periodic firebase",
                                        "firebasewas failed in periodically collecting places"
                                    )
                                }
                            }
                        });

                }

            } else {
                Log.d("jiplace create", "the keys are null")
            }

        }

        return Tasks.whenAll(taskUp, taskDown).onSuccessTask {
            var boolSrc: TaskCompletionSource<Boolean> = TaskCompletionSource<Boolean>();
            var boolTask: Task<Boolean> = boolSrc.getTask()
            boolSrc.setResult(true)
            boolTask
        }
    }

    override fun deleteMyPlaceOnServer(myPlace: MyPlace): Task<Void> {


        var fifteenMinGroupUp = Common.timeMinuteGroupUp(myPlace.time.time, 15).toString()
        var fifteenMinGroupDown = Common.timeMinuteGroupDown(myPlace.time.time, 15).toString()

        fun deletePerTimeGroup(timeGroup: String) {

            var jiplaceKeyIdRef = FirebaseDatabase
                .getInstance().getReference("myplaceusers/$theFbId/$timeGroup")

            val jiplaceKeyIdListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val jiplaceKeyId = dataSnapshot.getValue(String::class.java)
                    var childUpdates = HashMap<String, Any?>()
                    childUpdates["jiplaces/jiplacesKeyValue/$jiplaceKeyId"] = null
                    childUpdates["myplaceusers/$theFbId/${myPlace.uuidString}"] = null
                    childUpdates["myplaceusers/$theFbId/$timeGroup"] = null
                    childUpdates["myplaceusers/$theFbId/$jiplaceKeyId/hint"] = null
                    childUpdates["myplaceusers/$theFbId/$jiplaceKeyId/dateAdded"] = null
                    childUpdates["jiplaces/fifteen/$fifteenMinGroupUp/$jiplaceKeyId"] = null
                    childUpdates["jiplaces/fifteen/$fifteenMinGroupDown/$jiplaceKeyId"] = null
                    ref.updateChildren(childUpdates)

                }

                override fun onCancelled(p0: DatabaseError) {
                    return
                }
            }
            jiplaceKeyIdRef.addValueEventListener(jiplaceKeyIdListener)
        }
        deletePerTimeGroup(fifteenMinGroupDown)
        deletePerTimeGroup(fifteenMinGroupUp)
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

        return ref.updateChildren(childUpdates)
    }

    fun generateFirebaseItemKey(dbRefUrl: String): String? {
        var dbRef = FirebaseDatabase
            .getInstance()
            .getReference(dbRefUrl);
        var key = dbRef.push().key
        if (key == null) {
            Log.d("generateFbItemKey", "Couldn't get push key for posts")
            return null
        } else {
            Log.d("generateFbItemKey", "generated key for posts")
            return key
        }
    }
}

fun createMyPlaceHashMap(
    firebaseId: String,
    roundedJiplaceKey: String,
    timeGroup: String,
    myPlace: MyPlace
): HashMap<String, Any> {
    var myPlaceUuidKey = FirebaseDatabase.getInstance()
        .getReference("myplaceusers/$firebaseId/${myPlace.uuidString}").push().key
    FirebaseDatabase.getInstance()
        .getReference("myplaceusers/$firebaseId/${myPlace.uuidString}").child(myPlaceUuidKey!!)
        .setValue(roundedJiplaceKey)
    val childUpdates = HashMap<String, Any>()
    childUpdates["jiplaces/jiplacesKeyValue/$roundedJiplaceKey"] = firebaseId
    childUpdates["myplaceusers/$firebaseId/$timeGroup"] = roundedJiplaceKey!!
    childUpdates["myplaceusers/$firebaseId/$roundedJiplaceKey/hint"] = myPlace.hint
    childUpdates["myplaceusers/$firebaseId/$roundedJiplaceKey/dateAdded"] =
        myPlace.dateAdded.toString()
    return childUpdates
}