package com.almikey.jiplace.database.dao

import androidx.room.*
import com.almikey.jiplace.model.OtherUser
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface OtherUserDao {
    @Query("SELECT * FROM otheruser")
    fun getAll(): Flowable<List<OtherUser>>

    @Query("SELECT * FROM otheruser WHERE firebaseUid=:theUuid")
    fun findByUuid(theUuid: String): Single<OtherUser>

    @Insert
    fun insertAll(vararg otherUser:OtherUser)

    @Update
    fun update(vararg otherUser:OtherUser)

    @Delete
    fun delete(vararg otherUser:OtherUser)
}