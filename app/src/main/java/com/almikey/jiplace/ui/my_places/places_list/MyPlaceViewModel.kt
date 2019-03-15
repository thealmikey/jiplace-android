package com.almikey.jiplace.ui.my_places.places_list

import androidx.lifecycle.ViewModel
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.repository.MyPlacesRepositoryImpl
import com.google.android.gms.tasks.Task

class MyPlaceViewModel(val myPlacesRepositoryImpl: MyPlacesRepositoryImpl) : ViewModel() {
    var myPlaces = myPlacesRepositoryImpl.findAll()

    fun addPlace(myPlace: MyPlace) = myPlacesRepositoryImpl.addMyPlace(myPlace)
    fun update(myPlace:MyPlace)= myPlacesRepositoryImpl.update(myPlace)
    fun findByUuid(uuid:String) = myPlacesRepositoryImpl.findByUuid(uuid)
    fun delete(myPlace: MyPlace)=myPlacesRepositoryImpl.delete(myPlace)
    fun deleteOnDatabaseAfterServerDelete(myPlace: MyPlace,task: Task<Void>) = myPlacesRepositoryImpl.deleteOnDatabaseAfterServerDelete(myPlace,task)
}