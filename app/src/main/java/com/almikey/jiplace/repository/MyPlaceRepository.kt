package com.almikey.jiplace.repository

import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.service.LocalStorageService.MyPlaceLocalService
import com.google.android.gms.tasks.Task
import io.reactivex.Flowable
import io.reactivex.Single

abstract class MyPlacesRepository(var myPlacesLocalService: MyPlaceLocalService){
    abstract fun findAll():Flowable<List<MyPlace>>
    abstract fun addMyPlace(myPlace: MyPlace):Single<List<Long>>
    abstract fun findByUuid(uuid:String): Flowable<MyPlace>
    abstract fun update(myPlace: MyPlace): Single<Int>
    abstract fun findByLocationData(mLatitude:Float,mLongitude:Float,theTime:Long):Flowable<List<MyPlace>>
    abstract fun delete(myPlace: MyPlace):Single<Int>
    abstract fun deleteOnDatabaseAfterServerDelete(myPlace: MyPlace, task: Task<Void>)
}