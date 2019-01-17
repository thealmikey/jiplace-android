package com.almikey.jiplace.database

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import com.almikey.jiplace.model.MyPlace
import com.almikey.myplace.service.MyPlacesDao
import androidx.room.Room
import androidx.room.TypeConverters
import com.almikey.jiplace.util.DateTypeConverter


@Database(entities = arrayOf(MyPlace::class), version = 1)
@TypeConverters(DateTypeConverter::class)
abstract class MyPlacesRoomDatabase : RoomDatabase() {

    abstract fun myPlacesDao(): MyPlacesDao

}