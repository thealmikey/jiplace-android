package com.almikey.jiplace.service.ServerSyncService

import com.almikey.jiplace.model.MyPlace
import com.google.android.gms.tasks.Task

interface MyPlaceServerSyncService {
    fun createMyPlacesOnServer(vararg myPlace: MyPlace): Task<Boolean>
    fun deleteMyPlaceOnServer(myPlace: MyPlace):Task<Void>
   //TODO fun fetchMyPlacesFromServer(userId:String):Observable<List<MyPlace>>
}