package com.almikey.jiplace.repository

import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.service.MyPlaceLocalService.MyPlaceLocalService
import io.reactivex.Flowable

abstract class MyPlacesRepository(var myPlacesLocalService: MyPlaceLocalService){
    abstract fun findAll():Flowable<List<MyPlace>>
    abstract fun addMyPlace(jiplace: MyPlace)
    abstract fun findByUuid(uuid:String): Flowable<MyPlace>
    abstract fun update(myPlace: MyPlace)
    abstract fun findByLocationData(mLatitude:Float,mLongitude:Float,theTime:Long):Flowable<List<MyPlace>>
}