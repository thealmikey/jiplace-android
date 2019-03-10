package com.almikey.jiplace.util

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.almikey.jiplace.repository.MyPlacesRepository
import com.almikey.jiplace.worker.MyLocationWorker
import com.almikey.jiplace.worker.TimePickerWorker
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

class TimePickerFragment() : DialogFragment(), TimePickerDialog.OnTimeSetListener {

    val myPlacesRepo: MyPlacesRepository by inject()
    var theDate: String? = null
    var theUuid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        theDate = arguments?.getString("theDate")
        theUuid = arguments?.getString("theUuid")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
    }
@SuppressLint("AutoDispose")
    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        var theNewDate: Date = SimpleDateFormat("dd/MM/yyyy").parse(theDate);
        var dateCalendar: Calendar = Calendar.getInstance()

        dateCalendar.time = theNewDate
        dateCalendar.set(Calendar.HOUR, hourOfDay)
        dateCalendar.set(Calendar.MINUTE, minute)

        var theTime = dateCalendar.time
        var theTimeStr = theTime.time.toString()
        Log.d("a new day","celine $theTime")

//         myPlacesRepo.findByUuid(theUuid!!)
//            .observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe {
//                var newPlace = it.copy(time = theTime,timeRoundDown = timeMinuteGroupDown(theTime.time,15),timeRoundUp = timeMinuteGroupUp(theTime.time,15))
//                myPlacesRepo.update(newPlace)
//                Log.d("jiplace other","n putting a date in jiplace other")
//            }

        var timePickWorker = OneTimeWorkRequestBuilder<TimePickerWorker>().addTag("time-picker").
            setInputData(
                Data.Builder()
                    .putString("UuidKey", theUuid).putLong("theDate",theTime.time).build()
            )
            .build()
        Log.d("i went","past time picker worker")
        WorkManager.getInstance().enqueue(timePickWorker)
    }
}