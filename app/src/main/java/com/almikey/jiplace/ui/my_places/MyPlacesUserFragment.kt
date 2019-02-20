package com.almikey.jiplace.ui.my_places


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.EmptyResultSetException
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import co.chatsdk.core.dao.User
import co.chatsdk.core.dao.Thread
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.firebase.wrappers.UserWrapper

import com.almikey.jiplace.R
import com.almikey.jiplace.database.MyPlacesRoomDatabase
import com.almikey.jiplace.database.dao.MyPlaceUserSharedDao
import com.almikey.jiplace.database.dao.OtherUserDao
import com.almikey.jiplace.model.MyPlaceUserShared
import com.almikey.jiplace.model.OtherUser
import com.almikey.jiplace.ui.call.AudioCallActivity
import com.almikey.jiplace.util.ThreadCleanUp
import com.almikey.jiplace.util.ThreadCleanUp.deleteThreadsFromOtherSide
import com.almikey.jiplace.worker.DeleteThreadByOtherWorker
import com.almikey.myplace.service.MyPlacesDao
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryDataEventListener
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.Item
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.jiplaces_users_inplace_user_item.view.*
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class MyPlacesUserFragment : Fragment() {

    val mOtherUserDao: OtherUserDao by inject()
    val myPlacesDao: MyPlacesDao by inject()
    val myPlaceUserSharedDao: MyPlaceUserSharedDao by inject()
    public val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    lateinit var mRecyclerview: RecyclerView

    var theLatitude: Double? = null
    var theLongitude: Double? = null
    var theTime: Long = 0L
    var theUUID: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theLatitude = arguments?.getDouble("latitude")
        theLongitude = arguments?.getDouble("longitude")
        theTime = arguments!!.getLong("theTime")
        theUUID = arguments!!.getString("theUUID")

        deleteThreadsFromOtherSide(scopeProvider)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.jiplaces_users_inplace_recyclerview, container, false)
    }

    @SuppressLint("AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRecyclerview = view.findViewById(R.id.jiplace_users_inplace_recyclerview) as RecyclerView
        mRecyclerview.layoutManager = LinearLayoutManager(activity as Activity)
        val groupAdapter = GroupAdapter<ViewHolder>()

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

        var fifteenMinGroupUp = timeMinuteGroupUp(theTime!!, 15).toString()
        var fifteenMinGroupDown = timeMinuteGroupDown(theTime!!, 15).toString()

        fun nearByPeopleObservableRoundUp(): Observable<String> = Observable.create<String> { emitter ->
            var ref1: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("jiplaces/fifteen/$fifteenMinGroupUp")
            var geoFire: GeoFire = GeoFire(ref1);
            var geoQuery = geoFire.queryAtLocation(GeoLocation(theLatitude!!, theLongitude!!), 0.2);


            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String?, location: GeoLocation?) {
                    Log.d("geofire firebase", "detected a data entered")
                    Log.d("geofire onEntered", "$key")
                    emitter.onNext(key!!)
                    return
                }

                override fun onKeyMoved(key: String?, location: GeoLocation?) {
                    Log.d("geofire firebase", "moved")
                    Log.d("geofire on keymoved", "$key")
                    // groupAdapter.add(JiplaceUserItem(this@MyPlacesUserFragment))
                    return
                }

                override fun onKeyExited(key: String?) {
                    return
                }

                override fun onGeoQueryReady() {
                    Log.d("geofire query ready", "callbacks have been called")
                    Log.d("geofire url", "jiplaces/one/$theTime")
                    Log.d("longitude", "longitude is $theLongitude")
                    Log.d("latitude", "latitude is $theLatitude")
                    emitter.onComplete()
                    return
                }

                override fun onGeoQueryError(error: DatabaseError?) {
                    Log.d("geofire error", "geofire error ${error?.message}")
                    emitter.onError(error as Throwable)
                }
            });

        }

        fun nearByPeopleObservableRoundDown(): Observable<String> = Observable.create<String> { emitter ->
            var ref1: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("jiplaces/fifteen/$fifteenMinGroupDown")
            var geoFire: GeoFire = GeoFire(ref1);
            var geoQuery = geoFire.queryAtLocation(GeoLocation(theLatitude!!, theLongitude!!), 0.2);


            geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                override fun onKeyEntered(key: String?, location: GeoLocation?) {
                    Log.d("geofire firebase", "detected a data entered")
                    Log.d("geofire onEntered", "$key")
                    emitter.onNext(key!!)
                    return
                }

                override fun onKeyMoved(key: String?, location: GeoLocation?) {
                    Log.d("geofire firebase", "moved")
                    Log.d("geofire on keymoved", "$key")
                    // groupAdapter.add(JiplaceUserItem(this@MyPlacesUserFragment))
                    return
                }

                override fun onKeyExited(key: String?) {
                    return
                }

                override fun onGeoQueryReady() {
                    Log.d("geofire query ready", "callbacks have been called")
                    Log.d("geofire url", "jiplaces/one/$theTime")
                    Log.d("longitude", "longitude is $theLongitude")
                    Log.d("latitude", "latitude is $theLatitude")
                    emitter.onComplete()
                    return
                }

                override fun onGeoQueryError(error: DatabaseError?) {
                    Log.d("geofire error", "geofire error ${error?.message}")
                    emitter.onError(error as Throwable)
                }
            });

        }

        fun nearByPeopleObservable() = nearByPeopleObservableRoundDown().mergeWith(nearByPeopleObservableRoundUp()).distinct()
        //we use distinct to ensure that if someone jiplaces in the same place more than once, it doesn't appear
        //as two cards in the observer's

        var myDisposable = nearByPeopleObservable().observeOn(Schedulers.io())
            .distinct().filter {
                //                it != ChatSDK.currentUser().entityID
                it != FirebaseAuth.getInstance().uid
            }
            .subscribe { key ->
                mOtherUserDao.findByUuid(key).subscribe({
                    myPlaceUserSharedDao.findByMyPlaceUuid(theUUID)
                    Log.d("exisiting user", "${it.firebaseUid}")
                    Log.d("the UUID is", "${theUUID} existing user")
                    myPlaceUserSharedDao.findByUuidAndMyPlaceKey(theUUID, key).subscribe({
                        myPlaceUserSharedDao.findByMyPlaceUuid(theUUID)
                        Log.d("exisitingsharedPlace", "${it.myPlaceSharedId}")
                    }, { err ->
                        if (err is EmptyResultSetException) {
                            Log.d("user exists", "bt user shared no more creating them now")
                            myPlaceUserSharedDao.insertAll(
                                MyPlaceUserShared(
                                    otherUserId = key,
                                    sharedJiplaces = theUUID
                                )
                            )
                            Log.d("the UUID is", "${theUUID} new user")
                        }
                    })
                }, { err ->
                    if (err is EmptyResultSetException) {
                        Log.d("new user", "creating them now")
                        mOtherUserDao.insertAll(OtherUser(firebaseUid = key))
                        myPlaceUserSharedDao.insertAll(MyPlaceUserShared(otherUserId = key, sharedJiplaces = theUUID))
                        Log.d("the UUID is", "${theUUID} new user")
                    }
                })
            }

        nearByPeopleObservable().observeOn(Schedulers.io())
            .distinct()
            .autoDisposable(scopeProvider)
            .subscribe { userr ->

                Log.d("user frag", "user gotten $userr")
//                var fbId = ChatSDK.currentUser().entityID
                //get the images we need for the jiplacer pic


                myPlacesDao.findByUuid(theUUID).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe { myplace ->

                        Log.d("user frag", "person got ${myplace.hint}")
                        var fifteenMinGroupUp = myplace.timeRoundUp
                        var fifteenMinGroupDown = myplace.timeRoundDown


                        var refUpPic: DatabaseReference =
                            FirebaseDatabase.getInstance()
                                .getReference("myplaceusers/$userr/profilepic/$fifteenMinGroupUp")

                        var refDownPic: DatabaseReference =
                            FirebaseDatabase.getInstance()
                                .getReference("myplaceusers/$userr/profilepic/$fifteenMinGroupDown")


                        fun refUpPicObservable() = Observable.create<ArrayList<String>> { emitter ->
                            refUpPic.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    var theArr = arrayListOf<String>()
                                    if (dataSnapshot.childrenCount > 0) {
                                        for (imageSnapshot in dataSnapshot.children) {
                                            theArr.add(imageSnapshot.value as String)
                                            Log.d("user frag", "image url up i got ${imageSnapshot.value}")
                                        }
                                        Log.d("the frag", "down ${theArr.toString()}")
                                    }
                                    emitter.onNext(theArr)
                                }

                                override fun onCancelled(p0: DatabaseError) {

                                }
                            })


                        }

                        fun refDownPicObservable() = Observable.create<ArrayList<String>> { emitter ->
                            refDownPic.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    var theArr = arrayListOf<String>()
                                    if (dataSnapshot.childrenCount > 0) {
                                        for (imageSnapshot in dataSnapshot.children) {
                                            theArr.add(imageSnapshot.value as String)
                                            Log.d("user frag", "image url up i got ${imageSnapshot.value}")
                                        }
                                        Log.d("the frag", "down ${theArr.toString()}")
                                    }
                                    emitter.onNext(theArr)
                                }

                                override fun onCancelled(p0: DatabaseError) {

                                }
                            })


                        }


                        refDownPicObservable().mergeWith(refUpPicObservable()).distinct().subscribe {

                            var wrapper: UserWrapper = UserWrapper.initWithEntityId(userr);
                            wrapper.metaOn();
                            wrapper.onlineOn();
                            var user = wrapper.getModel();


                            Log.d("username", "${userr}")
                            if (FirebaseAuth.getInstance().uid != userr) {
                                groupAdapter.add(JiplaceUserItem(this@MyPlacesUserFragment, theTime, user, userr!!, it))
                            }
                        }
                    }
            }


        mRecyclerview.adapter = groupAdapter
    }

    class JiplaceUserItem(
        var context: Fragment,
        var theTime: Long,
        var user: User,
        var theKey: String,
        var imageUrls: ArrayList<String>
    ) : Item() {
        public val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(context) }
        override fun bind(viewHolder: ViewHolder, position: Int) {

            var wrapper: UserWrapper = UserWrapper.initWithEntityId(theKey);
            var userObservable = wrapper.metaOn();
            wrapper.onlineOn();
            userObservable.observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(scopeProvider)
                .subscribe {
                    if (it.entityID!! != null) {

                        viewHolder.itemView.jiplaceChat.isEnabled = true
                    }
                }

            //load the first image from the images in the user/profile/{time}/uuid.png into
            //the imageview
            if (!imageUrls.isEmpty()) {
                Picasso.get().load(imageUrls[0])
                    .config(Bitmap.Config.RGB_565)
                    .fit()
                    .centerCrop()
                    .placeholder(R.drawable.myuser)
                    .error(R.drawable.myuser)
                    .into(viewHolder.itemView.myplace_user_pic);
                //set an onClick that shows other images associated with this jiplace for this timespan
                //will load a view pager that loads thet items in imageUrls one by one
                viewHolder.itemView.myplace_user_pic.setOnClickListener {
                    var b: Bundle = Bundle();
                    b.putStringArray("user_image_urls", imageUrls.toTypedArray());
                    var i: Intent = Intent(context.context!!, UserImageActivity::class.java)
                    i.putExtras(b);
                    context.activity!!.startActivity(i)
                }
            }

            viewHolder.itemView.jiplaceCall.setOnClickListener {
                var b: Bundle = Bundle();
                b.putString("other_user_to_call", user.entityID);
                var i: Intent = Intent(context.context!!, AudioCallActivity::class.java)
                i.putExtras(b);
                context.activity!!.startActivity(i)
            }


            viewHolder.itemView.jiplaceChat.setOnClickListener {
                Log.d("user entity id", "${user.entityID}")
                if (FirebaseAuth.getInstance().uid!! != null && FirebaseAuth.getInstance().uid!! != user.entityID) {
                    Log.d("uuid n", "is reached")
                    ChatSDK.thread().createThread("${user.entityID}-$theTime", user, ChatSDK.currentUser())
                        .observeOn(AndroidSchedulers.mainThread())
                        .autoDisposable(scopeProvider)
                        .subscribe(object : Consumer<Thread> {
                            override fun accept(thread: Thread) {
                                var fbId = FirebaseAuth.getInstance().uid
                                var refChatLink: DatabaseReference = FirebaseDatabase.getInstance()
                                    .getReference("myplaceusers/chat/$fbId/${user.entityID}")
                                refChatLink.setValue(true)
                                ChatSDK.ui()
                                    .startChatActivityForID(context.activity!!.applicationContext, thread.entityID);
                            }
                        }, object : Consumer<Throwable> {
                            override fun accept(throwable: Throwable) {
                                // Handle error
                            }
                        });
                }
            }
            //findNavController(context).navigate(R.id.myPlacesUserFragment)
        }

        override fun getLayout(): Int {
            return R.layout.jiplaces_users_inplace_user_item
        }

        override fun createViewHolder(itemView: View): ViewHolder {
            return super.createViewHolder(itemView)
        }

    }

}

//@foreighnkeyUsers
//if otheruser exists in  otherMyPlaces:
//        ondelete = foreighkey.remove(alluserInstances) - 1 instance
//
//return (if user exists,  or deleted else return present Userinstance)
