package com.almikey.jiplace.service.LocalStorageService

import com.almikey.jiplace.model.MyPlace
import com.almikey.myplace.service.MyPlacesDao
import io.reactivex.Flowable
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class MyPlaceLocalServiceRoomImpl:MyPlaceLocalService,KoinComponent{
    val myPlacesDao:MyPlacesDao by inject()

    override fun getAll(): Flowable<List<MyPlace>> {
       return myPlacesDao.getAll()
    }

    override fun loadAllByIds(myplaceIds: IntArray): Flowable<List<MyPlace>> {
        return myPlacesDao.loadAllByIds(myplaceIds)
    }

    override fun findByHint(hint: String): Flowable<List<MyPlace>> {
        return myPlacesDao.findByHint(hint)
    }

    override fun findByUuid(theUuid: String): Flowable<MyPlace> {
        return myPlacesDao.findByUuid(theUuid)
    }

    override fun findByServerSyncStatus(workSync: Boolean, firebaseSync: Boolean): Flowable<List<MyPlace>> {
        return myPlacesDao.findByFbSyncStatus(workSync,firebaseSync)
    }

    override fun findByLocationData(
        mLatitude: Float,
        mLongitude: Float,
        theTime: Long,
        deleteStatus: String
    ): Flowable<List<MyPlace>> {
        return myPlacesDao.findByLocationData(mLatitude,mLongitude,theTime,deleteStatus)
    }

    override fun insertAll(vararg myPlaces: MyPlace) {
        return myPlacesDao.insertAll(*myPlaces)
    }

    override fun update(vararg myPlace: MyPlace) {
        return myPlacesDao.update(*myPlace)
    }

    override fun delete(myPlace: MyPlace) {
        return myPlacesDao.delete(myPlace)
    }
}