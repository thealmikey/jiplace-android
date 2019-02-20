package com.almikey.jiplace.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.almikey.jiplace.repository.MyPlacesRepository
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.util.*

class TimePickerWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {
    val myPlacesRepo: MyPlacesRepository by inject()

    val uuidKey = inputData.getString("UuidKey")
    val theDate = inputData.getLong("theDate", 0)
    override fun doWork(): Result {
        runBlocking {
            Log.d("saving time", "in worker this date $theDate")
            var thePlace = myPlacesRepo.findByUuid(uuidKey!!)
                .observeOn(Schedulers.io()).blockingFirst()
            CompletableFromAction {
                var newPlace = thePlace.copy(
                    time = Date(theDate),
                    timeRoundDown = timeMinuteGroupDown(theDate, 15),
                    timeRoundUp = timeMinuteGroupUp(theDate, 15)
                )
                myPlacesRepo.update(newPlace)
            }.subscribeOn(Schedulers.io()).subscribe {
                Log.d("jiplace other", "n putting a date in jiplace other")
            }
        }
        return Result.success()
    }

    fun timeMinuteGroupUp(theTime: Long, min: Int): Long {
        var timeInSec = theTime.toFloat() / 1000
        var timeInMin = timeInSec / 60
        var timeIn15 = timeInMin / min
        var fixedTime = Math.floor(timeIn15.toDouble())
        var timeInMs = fixedTime * min * 60 * 1000
        return timeInMs.toLong()
    }

    fun timeMinuteGroupDown(theTime: Long, min: Int): Long {
        var timeInSec = theTime.toFloat() / 1000
        var timeInMin = timeInSec / 60
        var timeIn15 = timeInMin / min
        var fixedTime = Math.ceil(timeIn15.toDouble())
        var timeInMs = fixedTime * min * 60 * 1000
        return timeInMs.toLong()
    }
}