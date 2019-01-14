package com.almikey.jiplace.ui.my_places

import androidx.lifecycle.ViewModel
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.repository.MyPlacesRepository

class MyPlacesViewModel(val myPlacesRepository: MyPlacesRepository):ViewModel() {
    var myPlaces = myPlacesRepository.mAllMyPlaces

    fun addPlace(myPlace:MyPlace)= myPlacesRepository.addMyPlace(myPlace)

    }