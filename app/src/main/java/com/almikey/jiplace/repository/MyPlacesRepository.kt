package com.almikey.jiplace.repository

import android.app.Application
import com.almikey.jiplace.database.MyPlacesRoomDatabase

class MyPlacesRepository(var application: Application){
    var db:MyPlacesRoomDatabase = MyPlacesRoomDatabase.getDatabase(application)!!
    var myPlacesDao = db.myPlacesDao()
    var mAllMyPlaces = myPlacesDao.getAll()
}