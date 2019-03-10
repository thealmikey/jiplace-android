package com.almikey.jiplace.service.MyPlaceSearchService

import com.almikey.jiplace.model.MyLocation
import io.reactivex.Observable
import java.util.Date

typealias UserId = String

interface MyPlaceSearchService {
    fun findNearByPeopleObservable(time: Long, location: MyLocation): Observable<UserId>
}