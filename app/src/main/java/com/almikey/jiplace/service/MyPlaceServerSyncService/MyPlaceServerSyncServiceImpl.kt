package com.almikey.jiplace.service.MyPlaceServerSyncService

import com.almikey.jiplace.model.MyPlace

class MyPlaceServerSyncServiceImpl(var myPlaceServerSyncService: MyPlaceServerSyncService):MyPlaceServerSyncService {
    override fun createMyPlacesOnServer(vararg myPlace: MyPlace) {
        myPlaceServerSyncService.createMyPlacesOnServer(*myPlace)
    }

    override fun updateMyPlacesOnServer(vararg myPlace: MyPlace) {
        myPlaceServerSyncService.updateMyPlacesOnServer(*myPlace)
    }

    override fun deleteMyPlacesOnServer(vararg myPlace: MyPlace) {
       myPlaceServerSyncService.deleteMyPlacesOnServer(*myPlace)
    }
}