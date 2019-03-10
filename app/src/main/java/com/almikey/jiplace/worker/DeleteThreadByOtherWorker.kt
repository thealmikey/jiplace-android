package com.almikey.jiplace.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import co.chatsdk.core.dao.DaoCore
import co.chatsdk.core.interfaces.ThreadType
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.firebase.wrappers.UserWrapper
import com.almikey.jiplace.database.dao.MyPlaceUserSharedDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class DeleteThreadByOtherWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {


    val myPlaceUserSharedDao: MyPlaceUserSharedDao by inject()

    fun deleteThreadsFromOtherSide() {

        var fbId = FirebaseAuth.getInstance().uid

        var refChatLink: DatabaseReference = FirebaseDatabase.getInstance()
            .getReference("myplaceusers/chat/$fbId")

        var cc = refChatLink.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var theArr = arrayListOf<String>()
                if (dataSnapshot.childrenCount > 0) {
                    for (chatSnapshot in dataSnapshot.children) {
                        Log.d("chat snapshot", chatSnapshot.toString())
                        Log.d("chat value", chatSnapshot.value.toString())
                        Log.d("chat key", chatSnapshot.key.toString())
                        //chat status is a flag that tells us whether or not we should delete a chat with a user
                        //if false it means we should delete
                        var chatStatus = chatSnapshot.value as Boolean
                        if (chatStatus == false) {
                            var wrapper: UserWrapper = UserWrapper.initWithEntityId(chatSnapshot.key);
                            var userObservable = wrapper.metaOn();
                            var myUser =
                                userObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                                    .blockingFirst()
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

                            Log.d("deleted threads", "deleted threads from the other side")
                        }
                    }

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    override fun doWork(): Result {
        try {
            runBlocking {
                CompletableFromAction {
                    deleteThreadsFromOtherSide()
                }.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).blockingAwait()
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}