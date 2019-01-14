package com.almikey.jiplace.repository

import android.app.Application
import androidx.lifecycle.LiveData
import com.almikey.jiplace.database.MyPlacesRoomDatabase
import com.almikey.jiplace.model.MyPlace
import io.reactivex.Flowable

class MyPlacesRepository(var application: Application){
    val db = MyPlacesRoomDatabase.getDatabase(application)!!
    var myPlacesDao = db.myPlacesDao()
    var mAllMyPlaces: Flowable<List<MyPlace>> = myPlacesDao.getAll()
    fun addMyPlace(jiplace:MyPlace)=myPlacesDao.insertAll(jiplace)

}