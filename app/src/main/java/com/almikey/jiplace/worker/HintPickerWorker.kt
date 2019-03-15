package com.almikey.jiplace.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.almikey.jiplace.repository.MyPlacesRepositoryImpl
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class HintPickerWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {
    val myPlacesRepoImpl: MyPlacesRepositoryImpl by inject()

    val uuidKey = inputData.getString("UuidKey")!!
    val theHint = inputData.keyValueMap.get("hint").toString()


    override fun doWork(): Result {
        runBlocking {
         saveHintLocally(uuidKey,theHint)
        }
        return Result.success()
    }

    fun saveHintLocally(placeUuid:String,hintText:String){
        Log.d("saving loc", "worker this location")
        var thePlace = myPlacesRepoImpl.findByUuid(placeUuid!!).take(1)
            .observeOn(Schedulers.io()).blockingFirst()
        CompletableFromAction {
            var newPlace = thePlace.copy(hint = hintText)
            myPlacesRepoImpl.update(newPlace).subscribe()
        }.subscribeOn(Schedulers.io()).subscribe {
            Log.d("jiplace other", "n putting a location in jiplace other")
        }
    }
}