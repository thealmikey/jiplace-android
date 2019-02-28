package com.almikey.jiplace.ui.my_places.places_list

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.chatsdk.core.session.NM
import com.almikey.jiplace.model.MyPlace
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Environment
import android.widget.EditText
import androidx.work.*
import co.chatsdk.core.dao.DaoCore
import co.chatsdk.core.interfaces.ThreadType
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.firebase.wrappers.UserWrapper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.almikey.jiplace.R
import com.almikey.jiplace.database.dao.MyPlaceUserSharedDao
import com.almikey.jiplace.model.MyPlaceProfilePic
import com.almikey.jiplace.model.MyPlaceUserShared
import com.almikey.jiplace.repository.MyPlacesRepository
import com.almikey.jiplace.ui.my_places.ContextMenuRecyclerView
import com.almikey.jiplace.util.FilePickerUtil
import com.almikey.jiplace.util.ThreadCleanUp.deleteThreadsFromOtherSide
import com.almikey.jiplace.worker.UploadMyPlaceImageWorker
import com.almikey.myplace.service.MyPlacesDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import io.reactivex.subjects.PublishSubject


class MyPlacesFragment : Fragment(), KoinComponent {


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


    val myPlacesRepo: MyPlacesRepository by inject()
    val myPlacesDao: MyPlacesDao by inject()
    val myPlaceUserSharedDao: MyPlaceUserSharedDao by inject()

    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    val myPlacesViewModel: MyPlaceViewModel by viewModel()
    var myPlaces: MutableList<MyPlace> = mutableListOf()

    lateinit var mCoordinatorLayout: CoordinatorLayout

    lateinit var mRecyclerview: RecyclerView
    lateinit var myPlacesAdapter: MyPlaceAdapter

    val firebaseAuth by lazy { FirebaseAuth.getInstance() }

    var mySubject = PublishSubject.create<ContextMenuRecyclerView.RecyclerViewContextMenuInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NM.auth().authenticateWithCachedToken()
        return
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        deleteThreadsFromOtherSide(scopeProvider)

        Log.d(
            "external media", Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ).absolutePath
        )
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.jiplaces_recyclerview, container, false)
    }


    fun editMyPlace(myPlace: MyPlace) {

    }

    @SuppressLint("AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        mRecyclerview = view?.findViewById(R.id.jiplace_recyclerview) as RecyclerView
        mCoordinatorLayout = view?.findViewById(R.id.jiplaces_container_for_recyclerview) as CoordinatorLayout
        mRecyclerview = ContextMenuRecyclerView(this.context!!)
        var params: ViewGroup.LayoutParams =
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        mRecyclerview.layoutParams = params
        mCoordinatorLayout.addView(mRecyclerview)
        mRecyclerview.layoutManager = LinearLayoutManager(activity as Activity)

//        var mListener =  RvMyPlaceItemListener(this.context!!,mRecyclerview,MyPlaceClickListener(this.context!!))
//
//        mRecyclerview.addOnItemTouchListener(mListener)


        myPlacesAdapter = MyPlaceAdapter(this, myPlaces)

        myPlacesViewModel.myPlaces.observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(scopeProvider)
            .subscribe {
                myPlaces.clear()
                it.forEach {
                    if (it.deletedStatus == "false") {
                        myPlaces.add(it)
                    }

                }
                myPlacesAdapter.notifyDataSetChanged()
            }


        mRecyclerview.adapter = myPlacesAdapter
        registerForContextMenu(mRecyclerview);
        var mySubjectObservable: Observable<ContextMenuRecyclerView.RecyclerViewContextMenuInfo> = mySubject
//
//        @SuppressLint("AutoDispose")
//      var cc =  mySubjectObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe {info ->
//           myPlacesRepo.findByUuid(myPlaces[info.position].uuidString).subscribe {thePlace ->
//               var newPlace = thePlace.copy(deletedStatus = "pending")
//               myPlacesRepo.update(newPlace)
//               var usersInPlace: List<MyPlaceUserShared> = myPlaceUserSharedDao
//                   .findByMyPlaceUuid(thePlace.uuidString)
//                   .blockingFirst()
//               Log.d("users in place","${usersInPlace.size}")
//
//               var theFbId = firebaseAuth.uid!!
//               var ref: DatabaseReference = FirebaseDatabase
//                   .getInstance().reference
//
//               var fifteenMinGroupUp = timeMinuteGroupUp(thePlace.time.time, 15).toString()
////            var fiveMinGroupDown = timeMinuteGroupDown(it.time.time, 5).toString()
//               var fifteenMinGroupDown = timeMinuteGroupDown(thePlace.time.time, 15).toString()
//
//               val childUpdates = HashMap<String, Any?>()
//               childUpdates["jiplaces/fifteen/$fifteenMinGroupUp/$theFbId"] = null
//               childUpdates["jiplaces/fifteen/$fifteenMinGroupDown/$theFbId"] = null
//
//               ref.updateChildren(childUpdates).addOnSuccessListener {
//
//                   var newPlace = thePlace.copy(deletedStatus = "true")
//                   myPlacesDao.update(newPlace)
//                   var myPlacesShared: List<MyPlaceUserShared> = myPlaceUserSharedDao
//                       .findByMyPlaceUuid(thePlace.uuidString)
//                       .blockingFirst()
//                   myPlaceUserSharedDao.delete(*myPlacesShared.toTypedArray())
//                   for (i in usersInPlace) {
//                       Log.d("shared user", "i got in the loop ${usersInPlace.size}")
//                       var userTimes: Int = myPlaceUserSharedDao.findByMyPlaceUuid(thePlace.uuidString).blockingFirst().size
//                       Log.d("shared user", "the number of jiplaces shared is $userTimes")
//                       if (userTimes == 0) {
//                           var wrapper: UserWrapper = UserWrapper.initWithEntityId(i.otherUserId);
//                           var userObservable = wrapper.metaOn();
//
//                           var myUser = userObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).blockingFirst()
//                           if (myUser.entityID!! != null) {
//                               for (thread in ChatSDK.thread().getThreads(ThreadType.Private1to1)) {
//                                   if (thread.getUsers().size === 2 &&
//                                       thread.containsUser(ChatSDK.currentUser()) &&
//                                       thread.containsUser(myUser)
//                                   ) {
//                                       var jointThread = thread
//                                       DaoCore.deleteEntity(jointThread);
//                                       DaoCore.deleteEntity(myUser);
//                                   }
//                               }
//                           }
//
//                       }
//                   }
//
//               }
//
//
//           }
//        }


    }


    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = activity!!.menuInflater
        inflater.inflate(R.menu.jiplace_context_menu, menu)
    }

    @SuppressLint("AutoDispose")
    override fun onContextItemSelected(item: MenuItem): Boolean {
        Log.d("context menu item", "context menu item selected")
        val info = item.menuInfo as ContextMenuRecyclerView.RecyclerViewContextMenuInfo
        return when (item.itemId) {
            R.id.edit_jiplace_description -> {
                Log.d("edit", "context menu")
                getHintAfterJiplaceOther(myPlaces[info.position].uuidString, info.position)
                Toast.makeText(this.context, "edit ${info.position}", Toast.LENGTH_LONG).show()
                true
            }
            R.id.delete_jiplace -> {
                Log.d("delete", "context menu")
//                mySubject.onNext(info)

//                    {info ->
                myPlacesRepo.findByUuid(myPlaces[info.position].uuidString)
                    .subscribeOn(Schedulers.io()).take(1).observeOn(Schedulers.io()).subscribe { thePlace ->
                        var newPlace = thePlace.copy(deletedStatus = "pending")
                        myPlacesRepo.update(newPlace)
                        var usersInPlace: List<MyPlaceUserShared> = myPlaceUserSharedDao
                            .findByMyPlaceUuid(thePlace.uuidString)
                            .blockingFirst()
                        Log.d("users in place", "${usersInPlace.size}")

                        var theFbId = firebaseAuth.uid!!
                        var ref: DatabaseReference = FirebaseDatabase
                            .getInstance().reference

                        var fifteenMinGroupUp = timeMinuteGroupUp(thePlace.time.time, 15).toString()
//            var fiveMinGroupDown = timeMinuteGroupDown(it.time.time, 5).toString()
                        var fifteenMinGroupDown = timeMinuteGroupDown(thePlace.time.time, 15).toString()

                        val childUpdates = HashMap<String, Any?>()
                        childUpdates["jiplaces/fifteen/$fifteenMinGroupUp/$theFbId"] = null
                        childUpdates["jiplaces/fifteen/$fifteenMinGroupDown/$theFbId"] = null

                        ref.updateChildren(childUpdates).addOnSuccessListener {

                            CompletableFromAction {
                                var newPlace = thePlace.copy(deletedStatus = "true")
                                myPlacesDao.update(newPlace)
                                var myPlacesShared: List<MyPlaceUserShared> = myPlaceUserSharedDao
                                    .findByMyPlaceUuid(thePlace.uuidString)
                                    .blockingFirst()
                                myPlaceUserSharedDao.delete(*myPlacesShared.toTypedArray())
                                for (i in usersInPlace) {
                                    Log.d("shared user", "i got in the loop ${usersInPlace.size}")
                                    var userTimes: Int =
                                        myPlaceUserSharedDao.findByMyPlaceUuid(thePlace.uuidString).blockingFirst().size
                                    Log.d("shared user", "the number of jiplaces shared is $userTimes")
                                    if (userTimes == 0) {
                                        var wrapper: UserWrapper = UserWrapper.initWithEntityId(i.otherUserId);
                                        var userObservable = wrapper.metaOn();

                                        var myUser =
                                            userObservable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io())
                                                .blockingFirst()
                                        if (myUser.entityID!! != null) {
                                            for (thread in ChatSDK.thread().getThreads(ThreadType.Private1to1)) {
                                                if (thread.getUsers().size === 2 &&
                                                    thread.containsUser(ChatSDK.currentUser()) &&
                                                    thread.containsUser(myUser)
                                                ) {
                                                    var jointThread = thread
                                                    DaoCore.deleteEntity(jointThread);
                                                    DaoCore.deleteEntity(myUser);
                                                    var refUserChatLink: DatabaseReference =
                                                        FirebaseDatabase.getInstance()
                                                            .getReference("myplaceusers/chat/${firebaseAuth.uid}/${myUser.entityID}")
                                                    var refUserChatOtherLink: DatabaseReference =
                                                        FirebaseDatabase.getInstance()
                                                            .getReference("myplaceusers/chat/${myUser.entityID}/${firebaseAuth.uid}")
                                                    refUserChatLink.setValue(false)
                                                    refUserChatOtherLink.setValue(false)
                                                }
                                            }
                                        }

                                    }
                                }

                            }.subscribeOn(Schedulers.io()).subscribe()
                        }

                    }
                Toast.makeText(this.context, "$ delete {info.position}", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onContextItemSelected(item)
        }


        // handle menu item here
        return super.onContextItemSelected(item)
    }


    fun getHintAfterJiplaceOther(theUuid: String, position: Int) {
        lateinit var theHintStr: String
        var dialog = MaterialDialog(activity as Activity).show {
            customView(R.layout.jiplace_description_hint)
        }
        val customView = dialog.getCustomView()
        var theText = customView?.findViewById<EditText>(R.id.jiplaceDescription)
        theText?.text.toString()

        dialog.positiveButton {
            theHintStr = theText?.text.toString()
            CompletableFromAction {
                var thePlace = myPlacesRepo.findByUuid(theUuid)
                    .subscribeOn(Schedulers.io()).blockingFirst()
                var newPlace = thePlace.copy(hint = theHintStr)

                @SuppressLint("AutoDispose")
                var b = CompletableFromAction { myPlacesRepo.update(newPlace)
                    var thePlaces =  myPlaces.toMutableList()
                    thePlaces[position] = newPlace
                    myPlacesAdapter.myplaces = thePlaces
                }
                    .subscribeOn(Schedulers.io()).subscribe()
            }.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .autoDisposable(scopeProvider)
                .subscribe {

                    myPlacesAdapter.notifyItemChanged(position)
                    Log.d("jiplace other", "n putting a location in jiplace other")
                }
        }
    }

    @SuppressLint("AutoDispose")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {

                var uri = FilePickerUtil.getPath(context!!, data.getData())

                Log.d("local uri", "the local uri is $uri")
                var theUuid = myPlacesAdapter.uuidForSelectedImage
                var thePosition = myPlacesAdapter.thePosition
                // do you stuff here
                var thePlaces =  myPlaces.toMutableList()
                thePlaces[thePosition] = thePlaces[thePosition].copy(profile = MyPlaceProfilePic(localPicUrl = "placeholder"))
                myPlacesAdapter.myplaces = thePlaces
                mRecyclerview.adapter!!.notifyItemChanged(thePosition)
                myPlacesRepo.findByUuid(theUuid).take(1)
                    .observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe {
                        var newPlace = it.copy(profile = MyPlaceProfilePic(localPicUrl = uri!!))
                        //change item in db and change the adapter
                        Completable.fromAction {
                            myPlacesRepo.update(newPlace)
                        }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe {
                            myPlacesViewModel.myPlaces.observeOn(AndroidSchedulers.mainThread())
                                .autoDisposable(scopeProvider)
                                .subscribe {
                                    myPlaces.clear()
                                    it.forEach {
                                        if (it.deletedStatus == "false") {
                                            myPlaces.add(it)
                                        }

                                    }
                                    myPlacesAdapter.notifyDataSetChanged()
                                }
//                            mRecyclerview.adapter!!.notifyItemChanged(thePosition)
                            Log.d("adapter notify", "after loading adapter notify")
                        }
                        var constraint: Constraints =
                            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                        var uploadMyPlaceImageWorker =
                            OneTimeWorkRequestBuilder<UploadMyPlaceImageWorker>().addTag("place-picker").setInputData(
                                Data.Builder()
                                    .putString("UuidKey", it.uuidString)
                                    .putAll(
                                        mapOf(
                                            "fifteenMinGroupUp" to it.timeRoundUp,
                                            "fifteenMinGroupDown" to it.timeRoundDown,
                                            "localPicUri" to data.getData().toString()
                                        )
                                    )
                                    .build()
                            )
                                .setConstraints(constraint).build()
                        WorkManager.getInstance().enqueue(uploadMyPlaceImageWorker)
                    }
            } else {
                var thePosition = myPlacesAdapter.thePosition
                var thePlaces =  myPlaces.toMutableList()
                thePlaces[thePosition] = thePlaces[thePosition].copy(profile = MyPlaceProfilePic(localPicUrl = ""))
                myPlacesAdapter.myplaces = thePlaces
                mRecyclerview.adapter = MyPlaceAdapter(this,thePlaces)
                mRecyclerview.adapter!!.notifyItemChanged(thePosition)
            }
        }
    }


}


//    override fun onContextItemSelected(item: MenuItem): Boolean {
//
//        when (item.getItemId()) {
//            R.id.edit_jiplace_description -> {
//
//                return true
//            }
//            R.id.delete_jiplace -> {
//
//                return true
//            }
//        }
//        return super.onContextItemSelected(item);
//    }




