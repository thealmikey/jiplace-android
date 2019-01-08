package com.almikey.myplace.service

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.almikey.jiplace.model.MyPlace

@Dao
interface MyPlacesDao {
    @Query("SELECT * FROM myplace")
    fun getAll(): List<MyPlace>

    @Query("SELECT * FROM myplace WHERE jid IN (:myplaceIds)")
    fun loadAllByIds(myplaceIds: IntArray): List<MyPlace>

    @Query("SELECT * FROM myplace WHERE hint LIKE :hint  LIMIT 1")
    fun findByHint(hint: String, last: String): MyPlace

    @Insert
    fun insertAll(vararg myPlaces: MyPlace)

    @Delete
    fun delete(myPlace: MyPlace)
}