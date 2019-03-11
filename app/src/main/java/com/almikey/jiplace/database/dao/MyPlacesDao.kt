package com.almikey.myplace.service

import androidx.room.*
import com.almikey.jiplace.model.MyPlace
import com.firebase.geofire.GeoLocation
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface MyPlacesDao {
    @Query("SELECT * FROM myplace")
    fun getAll(): Flowable<List<MyPlace>>

    @Query("SELECT * FROM myplace WHERE jid IN (:myplaceIds)")
    fun loadAllByIds(myplaceIds: IntArray): Flowable<List<MyPlace>>

    @Query("SELECT * FROM myplace WHERE hint LIKE :hint  LIMIT 1")
    fun findByHint(hint: String): Flowable<List<MyPlace>>

    @Query("SELECT * FROM myplace WHERE uuidString=:theUuid")
    fun findByUuid(theUuid: String): Flowable<MyPlace>

    @Query("SELECT * FROM myplace WHERE workSync=:workSync AND firebaseSync=:firebaseSync")
    fun findByFbSyncStatus(workSync:Boolean =true,firebaseSync:Boolean =false): Flowable<List<MyPlace>>

    @Query("SELECT * FROM myplace WHERE deletedStatus=:deleteStatus AND latitude=:mLatitude AND longitude=:mLongitude" +
            " AND timeRoundDown=:theTime OR timeRoundUp=:theTime")
    fun findByLocationData(mLatitude:Float,mLongitude:Float,theTime:Long,deleteStatus:String="true"): Flowable<List<MyPlace>>

    @Insert
    fun insertAll(vararg myPlaces: MyPlace)

    @Update
    fun update(vararg myPlace:MyPlace)

    @Delete
    fun delete(myPlace: MyPlace)
}