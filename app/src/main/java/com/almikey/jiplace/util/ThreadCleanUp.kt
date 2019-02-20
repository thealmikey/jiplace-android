package com.almikey.jiplace.util

import android.location.Location
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import co.chatsdk.core.dao.DaoCore
import co.chatsdk.core.interfaces.ThreadType
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.firebase.wrappers.UserWrapper
import com.almikey.jiplace.database.dao.MyPlaceUserSharedDao
import com.almikey.jiplace.worker.DeleteThreadByOtherWorker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.uber.autodispose.ScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

object ThreadCleanUp : KoinComponent {

    val myPlaceUserSharedDao: MyPlaceUserSharedDao by inject()

    fun deleteThreadsFromOtherSide(scopeProvider: ScopeProvider) {

        var fbId = FirebaseAuth.getInstance().uid

        var refChatLink: DatabaseReference = FirebaseDatabase.getInstance()
            .getReference("myplaceusers/chat/$fbId")


        var deleteListObservable = Observable.create<ArrayList<DataSnapshot>> { emitter ->

            var cc = refChatLink.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var theArr = arrayListOf<DataSnapshot>()
                    if (dataSnapshot.childrenCount > 0) {
                        for (chatSnapshot in dataSnapshot.children) {
                            Log.d("chat snapshot", chatSnapshot.toString())
                            Log.d("chat value", chatSnapshot.value.toString())
                            Log.d("chat key", chatSnapshot.key.toString())
                            theArr.add(chatSnapshot)

                        }
                        emitter.onNext(theArr)
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }


        deleteListObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
            .autoDisposable(scopeProvider)
            .subscribe { deleteList ->
                for (chatSnapshot in deleteList) {
                    var chatStatus = chatSnapshot.value as Boolean
                    if (chatStatus == false) {
                        var wrapper: UserWrapper = UserWrapper.initWithEntityId(chatSnapshot.key);
                        var userObservable = wrapper.metaOn();

                        userObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                            .take(1)
                            .subscribe ({ myUser ->
                                for (thread in ChatSDK.thread().getThreads(ThreadType.Private1to1)) {
                                    if (thread.getUsers().size === 2 &&
                                        thread.containsUser(ChatSDK.currentUser()) &&
                                        thread.containsUser(myUser)
                                    ) {
                                        var jointThread = thread
                                        DaoCore.deleteEntity(jointThread);
                                        DaoCore.deleteEntity(myUser);
                                        var refUserChatLink: DatabaseReference = FirebaseDatabase.getInstance()
                                            .getReference("myplaceusers/chat/$fbId/${chatSnapshot.key}")
                                        refUserChatLink.setValue(null)
                                    }
                                }
                            },{e->
                                Log.d("user error","the error is ${e.message}")
                                Log.d("complete error",e.localizedMessage)

                            })
                        Log.d("deleted threads", "deleted threads from the other side")
                    }
                }
            }
    }

}

//var chatStatus = chatSnapshot.value as Boolean
//if (chatStatus == false) {
//    var wrapper: UserWrapper = UserWrapper.initWithEntityId(chatSnapshot.key);
//    var userObservable = wrapper.metaOn();
//    var myUser =
//        userObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
//            .blockingFirst()
//    for (thread in ChatSDK.thread().getThreads(ThreadType.Private1to1)) {
//        if (thread.getUsers().size === 2 &&
//            thread.containsUser(ChatSDK.currentUser()) &&
//            thread.containsUser(myUser)
//        ) {
//            var jointThread = thread
//            DaoCore.deleteEntity(jointThread);
//            DaoCore.deleteEntity(myUser);
//            var refUserChatLink: DatabaseReference = FirebaseDatabase.getInstance()
//                .getReference("myplaceusers/chat/$fbId/${chatSnapshot.key}")
//            refUserChatLink.setValue(null)
//        }
//    }
//
//    Log.d("deleted threads", "deleted threads from the other side")}