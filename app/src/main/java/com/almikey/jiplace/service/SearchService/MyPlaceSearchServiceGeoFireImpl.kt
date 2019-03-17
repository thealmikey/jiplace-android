package com.almikey.jiplace.service.SearchService

import android.util.Log
import com.almikey.jiplace.model.MyLocation
import com.almikey.jiplace.util.Common
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Observable

object MyPlaceSearchServiceGeoFireImpl:MyPlaceSearchService {

    override fun findNearByPeopleObservable(time: Long, location: MyLocation): Observable<UserId> {
        var theTime = time
        var theLatitude = location.latitude
        var theLongitude = location.longitude

        var fifteenMinGroupUp = Common.timeMinuteGroupUp(theTime!!, 15).toString()
        var fifteenMinGroupDown = Common.timeMinuteGroupDown(theTime!!, 15).toString()

        fun nearByPeopleObservableRoundBy(timeGroup:String): Observable<String> = Observable.create<String> { emitter ->
            var ref1: DatabaseReference =
                FirebaseDatabase.getInstance().getReference("jiplaces/fifteen/$timeGroup")
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
                    // groupAdapter.add(MyplaceUserItem(this@MyPlacesUserFragment))
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
                    emitter.onError(error!!.toException())
                }
            });

        }
        var nearByPeopleObservableRoundDown = nearByPeopleObservableRoundBy(fifteenMinGroupDown)
        var nearByPeopleObservableRoundUp = nearByPeopleObservableRoundBy(fifteenMinGroupUp)

        return nearByPeopleObservableRoundDown.mergeWith(nearByPeopleObservableRoundUp).distinct().filter {
            //                it != ChatSDK.currentUser().entityID
            it != FirebaseAuth.getInstance().uid
        }
    }
}