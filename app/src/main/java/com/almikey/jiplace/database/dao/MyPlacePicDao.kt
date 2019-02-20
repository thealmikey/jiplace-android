package com.almikey.jiplace.database.dao

import androidx.room.*
import com.almikey.jiplace.model.MyPlacePicture
import io.reactivex.Flowable
import io.reactivex.Single


//this code goes and stores things in the Myplace database and returns the resules

@Dao
interface MyPlacePicDao {
    @Query("SELECT * FROM myplacepicture")
    fun getAll(): Flowable<List<MyPlacePicture>>

    @Query("SELECT * FROM myplacepicture WHERE placeUUID=:theUuid")
    fun findByUuid(theUuid: String): Single<MyPlacePicture>

    @Insert
    fun insertAll(vararg myPlacePicture: MyPlacePicture)

    @Update
    fun update(vararg myPlacePicture: MyPlacePicture)

    @Delete
    fun delete(vararg myPlacePicture: MyPlacePicture)
}