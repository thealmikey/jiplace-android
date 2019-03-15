package com.almikey.jiplace.repository

import android.util.Log
import co.chatsdk.core.dao.DaoCore
import co.chatsdk.core.dao.User
import co.chatsdk.core.interfaces.ThreadType
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.firebase.wrappers.UserWrapper
import com.almikey.jiplace.database.dao.MyPlaceUserSharedDao
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.model.MyPlaceUserShared
import com.almikey.jiplace.service.LocalStorageService.MyPlaceLocalService
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.io.InvalidObjectException

//

class MyPlacesRepositoryImpl(var myPlaceLocalService: MyPlaceLocalService,
                             var myPlaceUserSharedDao: MyPlaceUserSharedDao) : MyPlacesRepository(myPlaceLocalService){


    override fun findAll(): Flowable<List<MyPlace>> {
        return myPlaceLocalService.getAll()
    }

    override fun addMyPlace(myPlace: MyPlace):Single<List<Long>> {
      return  myPlaceLocalService.insertAll(myPlace)
    }

    override fun findByUuid(uuid: String): Flowable<MyPlace> {
        return myPlaceLocalService.findByUuid(uuid)
    }

    override fun update(myPlace: MyPlace): Single<Int> {
        Log.d("myPlacesRepositoryImp","update method called")
        return myPlaceLocalService.update(myPlace)
    }

    override fun findByLocationData(mLatitude: Float, mLongitude: Float, theTime: Long): Flowable<List<MyPlace>> {
        return myPlaceLocalService.findByLocationData(mLatitude, mLongitude, theTime)
    }

    override fun delete(myPlace: MyPlace): Single<Int> {
        if (myPlace.deletedStatus == "true") {
            Log.d("delete on db","i got to the delete method of repoImpl")
            return myPlaceLocalService.delete(myPlace).doOnSuccess {
                Log.d("delete method","delete method from MyPlaceRepoImpl")
            }
        }
        else{
           return Single.error<Int>(InvalidObjectException("the deleted status has to be true to delete"))
        }
    }

    override fun deleteOnDatabaseAfterServerDelete(myPlace: MyPlace, deleteTask: Task<Void>) {
        deleteTask.addOnSuccessListener {
            Log.d("delete on db","i did a server delete, now for local")
            CompletableFromAction {
                var newPlace = myPlace.copy(deletedStatus = "true")
               update(newPlace).subscribe({
                   Log.d("update success","set del status to true")
                   deleteOtherUsersFromDeletedPlace(myPlace.uuidString, myPlaceUserSharedDao)
               },{})
            }.subscribeOn(Schedulers.io()).subscribe {
                var delMyPlace = findByUuid(myPlace.uuidString).blockingFirst()
                Log.d("delOnDbafterSvrDelete","where the delete method is")
                delete(delMyPlace).subscribe({
                    Log.d("after del success","i managed to delete from db")
                },{
                    Log.d("after del error","i was not able to delete from db ${it.message!!}")
                })
            }
        }.addOnFailureListener {
            Log.d("delete on db","couldn do a server delete")
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
                            setChatOffForBothUsersOnFirebase(ChatSDK.currentUser(),otherUser)
                        }
                    }
                }

            }
        }
    }

    fun setChatOffForBothUsersOnFirebase(localUser: User, otherUser:User){
        var refUserChatLink: DatabaseReference =
            FirebaseDatabase.getInstance()
                .getReference("myplaceusers/chat/${localUser.entityID!!}/${otherUser.entityID}")
        var refUserChatOtherLink: DatabaseReference =
            FirebaseDatabase.getInstance()
                .getReference("myplaceusers/chat/${otherUser.entityID}/${localUser.entityID!!}")
        refUserChatLink.setValue(false)
        refUserChatOtherLink.setValue(false)
    }

}