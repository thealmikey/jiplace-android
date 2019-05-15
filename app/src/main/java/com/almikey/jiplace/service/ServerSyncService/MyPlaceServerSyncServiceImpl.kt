package com.almikey.jiplace.service.ServerSyncService

import com.almikey.jiplace.model.MyPlace
import com.google.android.gms.tasks.Task

class MyPlaceServerSyncServiceImpl(var myPlaceServerSyncService: MyPlaceServerSyncService):MyPlaceServerSyncService {
    override fun deleteMyPlaceOnServer(myPlace: MyPlace): Task<Void> {
        return myPlaceServerSyncService.deleteMyPlaceOnServer(myPlace)
    }

    override fun createMyPlacesOnServer(vararg myPlace: MyPlace) {
        return myPlaceServerSyncService.createMyPlacesOnServer(*myPlace)
    }

}