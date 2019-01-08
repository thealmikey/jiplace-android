package com.almikey.jiplace.service

import com.almikey.jiplace.model.MyPlace


interface MyPlacesService{
    fun createJiplace(myPlace: MyPlace)
    fun deleteJiplace(myPlace: MyPlace)
    fun updateJiplace(myPlace: MyPlace)
    fun getAllJiplace()
}