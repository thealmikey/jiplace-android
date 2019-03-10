package com.almikey.jiplace.service.MyPlaceService

import com.almikey.jiplace.model.MyPlace


interface MyPlacesService{
    fun createPlaceNow(myPlace: MyPlace)
    fun createPlaceOther(myPlace: MyPlace)
}