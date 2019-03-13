package com.almikey.jiplace.service.LocalStorageService

import com.almikey.jiplace.model.MyPlace
import io.reactivex.Flowable


interface MyPlaceLocalService {

    fun getAll(): Flowable<List<MyPlace>>

    fun loadAllByIds(myplaceIds: IntArray): Flowable<List<MyPlace>>

    fun findByHint(hint: String): Flowable<List<MyPlace>>

    fun findByUuid(theUuid: String): Flowable<MyPlace>

    fun findByServerSyncStatus(workSync:Boolean =true,firebaseSync:Boolean =false): Flowable<List<MyPlace>>

    fun findByLocationData(mLatitude:Float,mLongitude:Float,theTime:Long,deleteStatus:String="true"): Flowable<List<MyPlace>>

    fun insertAll(vararg myPlaces: MyPlace)

    fun update(vararg myPlace:MyPlace)

    fun delete(myPlace: MyPlace)
}