package com.almikey.jiplace.ui.my_places.users_list


import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.EmptyResultSetException
import co.chatsdk.firebase.wrappers.UserWrapper
import com.almikey.jiplace.R
import com.almikey.jiplace.database.dao.MyPlaceUserSharedDao
import com.almikey.jiplace.database.dao.OtherUserDao
import com.almikey.jiplace.model.MyLocation
import com.almikey.jiplace.model.MyPlaceUserShared
import com.almikey.jiplace.model.OtherUser
import com.almikey.jiplace.service.SearchService.MyPlaceSearchServiceGeoFireImpl.findNearByPeopleObservable
import com.almikey.jiplace.util.Common.timeMinuteGroupDown
import com.almikey.jiplace.util.Common.timeMinuteGroupUp
import com.almikey.jiplace.util.ThreadCleanUp.deleteThreadsFromOtherSide
import com.google.firebase.database.*
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.kotlinandroidextensions.ViewHolder
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject


class MyPlacesUserFragment : Fragment() {

    val mOtherUserDao: OtherUserDao by inject()
    val myPlaceUserSharedDao: MyPlaceUserSharedDao by inject()
    public val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    lateinit var mRecyclerview: RecyclerView

    var theLatitude: Double? = null
    var theLongitude: Double? = null
    var theTime: Long = 0L
    var theUUID: String = ""
    var fifteenMinGroupUp: Long = 0L
    var fifteenMinGroupDown: Long = 0L
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theLatitude = arguments?.getDouble("latitude")
        theLongitude = arguments?.getDouble("longitude")
        theTime = arguments!!.getLong("theTime")
        theUUID = arguments!!.getString("theUUID")
        if (theTime != 0L) {
            fifteenMinGroupUp = timeMinuteGroupUp(theTime, 15)
            fifteenMinGroupDown = timeMinuteGroupDown(theTime, 15)
        }
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
       // val groupAdapter = GroupAdapter<ViewHolder>()

        //we use distinct to ensure that if someone jiplaces in the same place more than once, it doesn't appear
        //as two cards in the observer's

        var findNearbyPeople = findNearByPeopleObservable(theTime, MyLocation(theLongitude!!, theLatitude!!))

        var myPlaceUsers:ArrayList<MyPlaceUser> = arrayListOf()

        //.autoDisposable(scopeProvider)
        findNearbyPeople
            .flatMap{ otherUser ->
                var wrapper: UserWrapper = UserWrapper.initWithEntityId(otherUser);
                wrapper.metaOn();
                wrapper.onlineOn();
                var user = wrapper.getModel();
                addNearbyUserToDbAsSharedUser(otherUser)

                fetchOtherUserMyPlacePicsObserverble(otherUser).distinct().flatMap {myPlacePics ->
                    getHintFromTimeAndUuid(otherUser, fifteenMinGroupUp.toString())
                        .mergeWith(getHintFromTimeAndUuid(otherUser, fifteenMinGroupDown.toString())).distinct()
                       .subscribeOn(Schedulers.io())
                       .map {
                           var myPlaceUser = MyPlaceUser(
                               "$it",
                               theTime,
                               user,
                               otherUser!!,
                               myPlacePics
                           )
                           myPlaceUsers.add(myPlaceUser)
                           myPlaceUsers
                       }
                }

            }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { myPlaceUsers ->
                mRecyclerview.adapter = MyPlaceUserAdapter(this@MyPlacesUserFragment,myPlaceUsers)
                mRecyclerview.adapter!!.notifyDataSetChanged()
                Log.d("myPlaceUsers","i added a user n notified dataset")
            }
        mRecyclerview.adapter = MyPlaceUserAdapter(this@MyPlacesUserFragment,myPlaceUsers)
    }


    fun fetchOtherUserMyPlacePicsObserverble(userRef: String): Observable<ArrayList<String>> {

        fun picReference(timeGroup: Long): DatabaseReference {
            return FirebaseDatabase.getInstance()
                .getReference("myplaceusers/$userRef/profilepic/$timeGroup")
        }

        var refUpPic: DatabaseReference = picReference(fifteenMinGroupUp)
        var refDownPic: DatabaseReference = picReference(fifteenMinGroupDown)

        fun refPicObservable(picRef: DatabaseReference) = Observable.create<ArrayList<String>> { emitter ->
            picRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var theArr = arrayListOf<String>()
                    if (dataSnapshot.childrenCount > 0) {
                        for (imageSnapshot in dataSnapshot.children) {
                            theArr.add(imageSnapshot.value as String)
                            Log.d("user frag", "image url up i got ${imageSnapshot.value}")
                        }
                    }
                    emitter.onNext(theArr)
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }

        var refDownPicObservable = refPicObservable(refDownPic)
        var refUpPicObservable = refPicObservable(refUpPic)

        return refDownPicObservable.mergeWith(refUpPicObservable)
    }

    @SuppressLint("AutoDispose")
    fun addNearbyUserToDbAsSharedUser(otherUserId: String) {
        var key = otherUserId
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

    fun getHintFromTimeAndUuid(userId:String,time:String):Observable<String>{
      return  Observable.create<String>(){emitter->
          var theHintRefId = FirebaseDatabase
              .getInstance().getReference("myplaceusers/$userId/$time")

          val hintIdListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Post object and use the values to update the UI
                    val hintId = dataSnapshot.getValue(String::class.java)
                    var theHintRef = FirebaseDatabase
                        .getInstance().getReference("myplaceusers/$userId/$hintId/hint")

                    val hintListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            // Get Post object and use the values to update the UI
                            val hintText = dataSnapshot.getValue(String::class.java)
                            Log.d("the hint",hintText!!)
                           emitter.onNext(hintText!!)
                            // ...
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Getting Post failed, log a message
                            Log.w("getting hint text", "failed load hint itself :onCancelled", databaseError.toException())
                            // ...
                        }
                    }
                    theHintRef.addValueEventListener(hintListener)
                    // ...
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Post failed, log a message
                    Log.w("getting hint", "load hint id :onCancelled", databaseError.toException())
                    // ...
                }
            }
            theHintRefId.addValueEventListener(hintIdListener)
        }
    }
}