package com.almikey.jiplace.ui.my_places.places_list

import androidx.lifecycle.ViewModel
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.repository.MyPlacesRepository

class MyPlaceViewModel(val myPlacesRepository: MyPlacesRepository):ViewModel() {
    var myPlaces = myPlacesRepository.mAllMyPlaces

    fun addPlace(myPlace:MyPlace)= myPlacesRepository.addMyPlace(myPlace)

    }