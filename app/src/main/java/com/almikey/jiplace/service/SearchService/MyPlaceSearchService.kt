package com.almikey.jiplace.service.SearchService

import com.almikey.jiplace.model.MyLocation
import io.reactivex.Observable

typealias UserId = String

interface MyPlaceSearchService {
    fun findNearByPeopleObservable(time: Long, location: MyLocation): Observable<UserId>
}