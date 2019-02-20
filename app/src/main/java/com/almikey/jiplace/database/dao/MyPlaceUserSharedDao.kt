package com.almikey.jiplace.database.dao

import androidx.room.*
import com.almikey.jiplace.model.MyPlaceUserShared
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface MyPlaceUserSharedDao {
    @Query("SELECT * FROM myplaceusershared")
    fun getAll(): Flowable<List<MyPlaceUserShared>>

    @Query("SELECT * FROM myplaceusershared WHERE sharedJiplaces=:placeUuid")
    fun findByMyPlaceUuid(placeUuid: String): Flowable<List<MyPlaceUserShared>>

    @Query("SELECT * FROM myplaceusershared WHERE otherUserId=:userKey")
    fun findByUserKey(userKey:String): Flowable<List<MyPlaceUserShared>>

    @Query("SELECT * FROM myplaceusershared WHERE sharedJiplaces=:myPlaceKey and otherUserId=:theUuid")
    fun findByUuidAndMyPlaceKey(theUuid: String,myPlaceKey:String): Single<MyPlaceUserShared>

    @Insert
    fun insertAll(vararg myPlaceUserShared:MyPlaceUserShared)

    @Update
    fun update(myPlaceUserShared: MyPlaceUserShared)

    @Delete
    fun delete(vararg myPlaceUserShared: MyPlaceUserShared)
}