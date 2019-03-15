package com.almikey.jiplace.service.MyPlaceSearchService

import com.almikey.jiplace.model.MyLocation
import io.reactivex.Observable

typealias UserId = String

interface MyPlaceSearchService {
    fun findNearByPeopleObservable(time: Long, location: MyLocation): Observable<UserId>
}