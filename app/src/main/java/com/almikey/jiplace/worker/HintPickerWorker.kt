package com.almikey.jiplace.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.almikey.jiplace.model.MyLocation
import com.almikey.jiplace.repository.MyPlacesRepository
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*

class HintPickerWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {
    val myPlacesRepo: MyPlacesRepository by inject()

    val uuidKey = inputData.getString("UuidKey")
    val theHint = inputData.keyValueMap.get("hint").toString()


    override fun doWork(): Result {
        runBlocking {
            Log.d("saving loc", "worker this location")
            var thePlace = myPlacesRepo.findByUuid(uuidKey!!).take(1)
                .observeOn(Schedulers.io()).blockingFirst()
            CompletableFromAction {
                var newPlace = thePlace.copy(hint = theHint)
                myPlacesRepo.update(newPlace)
            }.subscribeOn(Schedulers.io()).subscribe {
                Log.d("jiplace other", "n putting a location in jiplace other")
            }
        }
        return Result.success()
    }
}