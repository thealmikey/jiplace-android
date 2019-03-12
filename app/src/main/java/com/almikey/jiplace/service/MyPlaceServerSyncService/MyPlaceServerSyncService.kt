package com.almikey.jiplace.service.MyPlaceServerSyncService

import com.almikey.jiplace.model.MyPlace

interface MyPlaceServerSyncService {
    fun createMyPlacesOnServer(vararg myPlace: MyPlace)
    fun updateMyPlacesOnServer(vararg myPlace: MyPlace)
    fun deleteMyPlacesOnServer(vararg myPlace: MyPlace)
   //TODO fun fetchMyPlacesFromServer(userId:String):Observable<List<MyPlace>>
}