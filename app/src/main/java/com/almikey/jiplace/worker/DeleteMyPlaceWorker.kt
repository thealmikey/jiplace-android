//package com.almikey.jiplace.worker
//
//import android.content.Context
//import android.util.Log
//import androidx.room.Room
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//import co.chatsdk.core.dao.DaoCore
//import co.chatsdk.core.dao.User
//import co.chatsdk.core.error.ChatSDKException
//import com.almikey.jiplace.database.MyPlacesRoomDatabase
//import com.almikey.jiplace.model.MyPlace
//import com.almikey.jiplace.model.MyPlaceUserShared
//import com.firebase.geofire.GeoFire
//import com.firebase.geofire.GeoLocation
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.database.DatabaseError
//import com.google.firebase.database.DatabaseReference
//import com.google.firebase.database.FirebaseDatabase
//import io.reactivex.internal.operators.completable.CompletableFromAction
//import io.reactivex.schedulers.Schedulers
//import kotlinx.coroutines.runBlocking
//import co.chatsdk.core.interfaces.ThreadType
//import co.chatsdk.core.session.ChatSDK
//import co.chatsdk.firebase.FirebaseNetworkAdapter
//import co.chatsdk.firebase.file_storage.FirebaseFileStorageModule
//import co.chatsdk.firebase.push.FirebasePushModule
//import co.chatsdk.firebase.wrappers.UserWrapper
//import co.chatsdk.ui.manager.BaseInterfaceAdapter
//import com.uber.autodispose.autoDisposable
//import io.reactivex.Observable
//import io.reactivex.android.schedulers.AndroidSchedulers
//import java.util.*
//
//
//class DeleteMyPlaceWorker(context: Context, workerParameters: WorkerParameters) :
//    Worker(context, workerParameters) {
//
//    val context = applicationContext
//    val builder = co.chatsdk.core.session.Configuration.Builder(context)
//    var ab = builder.firebaseRootPath("prod")
//    var bc = builder.firebaseDatabaseURL("https://jiplace.firebaseio.com")
//    var cd = builder.setInboundPushHandlingEnabled(true)
//    var de = builder.setClientPushEnabled(true)
//   var ef = builder.reuseDeleted1to1Threads(false)
//   var fg = try {
//        ChatSDK.initialize(builder.build(), FirebaseNetworkAdapter(), BaseInterfaceAdapter(context)!!)
//    } catch (e: ChatSDKException) {
//    }
//   var gh = FirebaseFileStorageModule.activate()
//   var hj = FirebasePushModule.activate()
//
//
//    val myPlaceUuidKey = inputData.getString("UuidKey")
//    val thePlaceTime = inputData.getLong("theTime", 0L)
//
//    val myPlacesDb = Room.databaseBuilder(applicationContext, MyPlacesRoomDatabase::class.java, "myplaces-db")
//        .build()
//    val myPlaceUserSharedDao = myPlacesDb.myPlaceUserSharedDao()
//    val myPlacesDao = myPlacesDb.myPlacesDao()
//
//    lateinit var theFbId: String
//
//    var usersInPlace: List<MyPlaceUserShared> = myPlaceUserSharedDao
//        .findByMyPlaceUuid(myPlaceUuidKey!!)
//        .blockingFirst()
//    var bd = Log.d("users in place","${usersInPlace.size}")
//    var firebaseAuth = FirebaseAuth.getInstance()
//    var authStateListener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
//
//        theFbId = firebaseAuth.uid!!
//
//        fun timeMinuteGroupUp(theTime: Long, min: Int): Long {
//            var timeInSec = theTime.toFloat() / 1000
//            var timeInMin = timeInSec / 60
//            var timeIn15 = timeInMin / min
//            var fixedTime = Math.floor(timeIn15.toDouble())
//            var timeInMs = fixedTime * min * 60 * 1000
//            return timeInMs.toLong()
//        }
//
//        fun timeMinuteGroupDown(theTime: Long, min: Int): Long {
//            var timeInSec = theTime.toFloat() / 1000
//            var timeInMin = timeInSec / 60
//            var timeIn15 = timeInMin / min
//            var fixedTime = Math.ceil(timeIn15.toDouble())
//            var timeInMs = fixedTime * min * 60 * 1000
//            return timeInMs.toLong()
//        }
//
//
//        var fifteenMinGroupUp = timeMinuteGroupUp(thePlaceTime, 15).toString()
////            var fiveMinGroupDown = timeMinuteGroupDown(it.time.time, 5).toString()
//        var fifteenMinGroupDown = timeMinuteGroupDown(thePlaceTime, 15).toString()
//
//        var ref: DatabaseReference = FirebaseDatabase
//            .getInstance().reference
//
//        val childUpdates = HashMap<String, Any?>()
//        childUpdates["jiplaces/fifteen/$fifteenMinGroupUp/$theFbId"] = null
//        childUpdates["jiplaces/fifteen/$fifteenMinGroupDown/$theFbId"] = null
//
//        fun deleteMyPlaceWithThread(uuidKey: String) {
//
//
//            var thePlace: MyPlace = myPlacesDao.findByUuid(myPlaceUuidKey!!).blockingFirst()
//
//
//            CompletableFromAction {
//                var newPlace = thePlace.copy(deletedStatus = "true")
//                myPlacesDao.update(newPlace)
//                var myPlacesShared: List<MyPlaceUserShared> = myPlaceUserSharedDao.findByMyPlaceUuid(myPlaceUuidKey).blockingFirst()
//                myPlaceUserSharedDao.delete(*myPlacesShared.toTypedArray())
//            }.subscribeOn(Schedulers.io()).blockingAwait()
//
//
//            Log.d("jiplace other", "n putting a location in jiplace other")
//            Log.d("shared user", "i got out the loop ${usersInPlace.size}")
//            var usersInPlaceObservable = Observable.fromArray(*usersInPlace.toTypedArray())
//
//            //delete thread after deleting jiplace online
//            for (i in usersInPlace) {
//                Log.d("shared user", "i got in the loop ${usersInPlace.size}")
//                var userTimes: Int = myPlaceUserSharedDao.findByMyPlaceUuid(myPlaceUuidKey!!).blockingFirst().size
//                Log.d("shared user", "the number of jiplaces shared is $userTimes")
//                if (userTimes == 0) {
//                    var wrapper: UserWrapper = UserWrapper.initWithEntityId(i.otherUserId);
//                    var userObservable = wrapper.metaOn();
//
//                    var myUser = userObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).blockingFirst()
//                    if (myUser.entityID!! != null) {
//                        for (thread in ChatSDK.thread().getThreads(ThreadType.Private1to1)) {
//                            if (thread.getUsers().size === 2 &&
//                                thread.containsUser(ChatSDK.currentUser()) &&
//                                thread.containsUser(myUser)
//                            ) {
//                                var jointThread = thread
//                                DaoCore.deleteEntity(jointThread);
//                                DaoCore.deleteEntity(myUser);
//                            }
//                        }
//                    }
//
//                }
//            }
//        }
//        ref.updateChildren(childUpdates).addOnSuccessListener {
//            deleteMyPlaceWithThread(myPlaceUuidKey!!)
//        }
//    }
//
//    fun timeMinuteGroup(theTime: Long, min: Int): Long {
//        var timeInSec = theTime.toFloat() / 1000
//        var timeInMin = timeInSec / 60
//        var timeIn15 = timeInMin / min
//        var fixedTime = Math.floor(timeIn15.toDouble())
//        var timeInMs = fixedTime * min * 60 * 1000
//        return timeInMs.toLong()
//    }
//
//
//    var ref = FirebaseDatabase.getInstance().getReference()
//    var geoFire = GeoFire(ref)
//
//    override fun doWork(): Result {
//        try{
//            runBlocking {
//                CompletableFromAction {
//                    firebaseAuth.addAuthStateListener(authStateListener)
//                }.subscribeOn(Schedulers.io()).blockingAwait()
//            }
//            return Result.success()
//        }catch(e:Exception) {
//            Log.d("something wrong","unable to get location, retrying")
//            Result.retry()
//        }
//        return Result.failure()
//    }
//
//
//}