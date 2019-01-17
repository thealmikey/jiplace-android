package com.almikey.jiplace.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.almikey.jiplace.database.MyPlacesRoomDatabase
import com.almikey.jiplace.model.MyPlace
import io.reactivex.Flowable
import io.reactivex.Single

class MyPlacesRepository(var application: Application,var db:MyPlacesRoomDatabase){
    var myPlacesDao = db.myPlacesDao()
    var mAllMyPlaces: Flowable<List<MyPlace>> = myPlacesDao.getAll()
    fun addMyPlace(jiplace:MyPlace) = myPlacesDao.insertAll(jiplace)
    fun findByUuid(uuid:String):Flowable<MyPlace> = myPlacesDao.findByUuid(uuid)
    fun update(myPlace:MyPlace)= myPlacesDao.update(myPlace)


}