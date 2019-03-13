package com.almikey.jiplace.repository

import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.service.LocalStorageService.MyPlaceLocalService
import io.reactivex.Flowable

//

class MyPlacesRepositoryImpl(var myPlaceLocalService: MyPlaceLocalService) : MyPlacesRepository(myPlaceLocalService) {
    override fun findAll(): Flowable<List<MyPlace>> {
        return myPlaceLocalService.getAll()
    }

    override fun addMyPlace(jiplace: MyPlace) {
        myPlaceLocalService.insertAll(jiplace)
    }

    override fun findByUuid(uuid: String): Flowable<MyPlace> {
        return myPlaceLocalService.findByUuid(uuid)
    }

    override fun update(myPlace: MyPlace) {
        return myPlaceLocalService.update(myPlace)
    }

    override fun findByLocationData(mLatitude: Float, mLongitude: Float, theTime: Long): Flowable<List<MyPlace>> {
        return myPlaceLocalService.findByLocationData(mLatitude, mLongitude, theTime)
    }

    override fun delete(myPlace: MyPlace): Boolean {
        if (myPlace.deletedStatus == "true") {
            myPlaceLocalService.delete(myPlace)
            return true
        } else {
            return false
        }
    }
}