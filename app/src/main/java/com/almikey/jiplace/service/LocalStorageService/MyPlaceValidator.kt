package com.almikey.jiplace.service.LocalStorageService

import com.almikey.jiplace.model.MyPlace

object MyPlaceValidator {
   fun isPlaceNowValid(myPlace: MyPlace):Boolean{
        if(myPlace.uuidString.trim().isEmpty()){
            return false
        }
       return true
   }
}