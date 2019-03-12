package com.almikey.jiplace.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.almikey.jiplace.model.MyLocation
import com.almikey.jiplace.repository.MyPlacesRepositoryImpl
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class PlacePickerWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {
    val myPlacesRepoImpl: MyPlacesRepositoryImpl by inject()
    val uuidKey = inputData.getString("UuidKey")
    val theLocationMap = inputData.keyValueMap.get("location")
    var theLat = inputData.keyValueMap.get("latitude").toString()
    var theLon = inputData.keyValueMap.get("longitude").toString()
    //when a MyPlace is created immediately by JiPlace Now, the latitude and longitude fields both have
    //have 0.0 as their default values, this is because it takes a while before the device connects to
    //the satelite and get a true realtime accurate value, this worker is fired of once the location has been found
    //and it sets it in the database,by default the workSync flag on the MyPlace in the database at
    //this point is usually false to indicate that the place picker worker hasn't set the values yet
    override fun doWork(): Result {
        runBlocking {
            Log.d("saving loc", "worker this location")
            var thePlace = myPlacesRepoImpl.findByUuid(uuidKey!!)
                .observeOn(Schedulers.io()).blockingFirst()
            CompletableFromAction {
                var newPlace =
                    thePlace.copy(location = MyLocation(theLon!!.toDouble(), theLat!!.toDouble()), workSync = true)
                myPlacesRepoImpl.update(newPlace)
            }.subscribeOn(Schedulers.io()).subscribe {
                Log.d("jiplace other", "n putting a location in jiplace other")
            }
        }
        return Result.success()
    }
}