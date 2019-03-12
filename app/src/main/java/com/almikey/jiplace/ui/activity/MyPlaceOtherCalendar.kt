package com.almikey.jiplace.ui.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.repository.MyPlacesRepositoryImpl
import com.almikey.jiplace.ui.map.MyPlaceMapsActivity
import com.almikey.jiplace.util.TimePickerFragment
import com.almikey.jiplace.worker.PlacePickerWorker
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_crunchy_calendary.*
import ru.cleverpumpkin.calendar.CalendarView
import org.koin.android.ext.android.inject
import ru.cleverpumpkin.calendar.CalendarDate

import java.util.*

class MyPlaceOtherCalendar : AppCompatActivity() {

// a flag to indicate if a Jiplacing other event has been done to completion, this helps
    //when we start the timepicker fragment and place picker fragment which take us away from the activity
    //if the activity we left is cleaned up, we may lose the UUID we generated when we do
    var jiplaceCompleted: Boolean = false

    private val scopeProvider by lazy {
        AndroidLifecycleScopeProvider.from(this)
    }


    lateinit var theMainDate: Date;
    val myPlacesRepoImpl: MyPlacesRepositoryImpl by inject()
    lateinit var theUUId: String
    var theLateTime: Date? = null


    fun generateRandomUUID(): String {
        return UUID.randomUUID().toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crunchy_calendary)
        //If we're coming back to this Activity from TimePicker or from PlacePicker and have
        //to go through onCreate again, we check to confirm.
        //if the UUID is null it means we're coming here for the first time, if
        //the UUID has content inside, it means we were here and don't need to a new MyPlace instance
        //with createMyPlace
        if (savedInstanceState?.getString("theUuid") == null) {
            theUUId = generateRandomUUID()
            createMyPlace(theUUId)
        } else {
            theUUId = savedInstanceState.getString("theUuid")
        }

        var theCal = calendarDateView
        theCal.setupCalendar(selectionMode = CalendarView.SelectionMode.SINGLE)
        @SuppressWarnings
        theCal.onDateClickListener = { date ->
            launchTimePicker(date, theUUId)
            theMainDate = date.date

        }


        submitSelectedDate.setOnClickListener {
            submitSelectedDate.isEnabled = false
            var mapIntent: Intent = Intent(this, MyPlaceMapsActivity::class.java)
            startActivityForResult(mapIntent, 5);

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 5) {
            if (resultCode == RESULT_OK) {
                val theUuid = data?.getStringExtra("theUuid")
                var lat = data?.getDoubleExtra("latitude", 69.toDouble())
                var lon = data?.getDoubleExtra("longitude", 69.toDouble())
                var placeName = data?.getStringExtra("placeName")
                //launch a worker to store the activity from the Place Picker activity
                //to the database for the particular MyPlace which is identifiable through it's UUID
                launchPlacePickerWorker(lat!!,lon!!,theUUId)
                val resultIntent = Intent()
                resultIntent.putExtra("theUuid", "$theUUId")
                setResult(Activity.RESULT_OK, resultIntent)
                jiplaceCompleted = true
                finish()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        if (!jiplaceCompleted) {
            outState!!.putString("theUuid", theUUId);
        } else {
            outState!!.clear()
        }
    }

    fun createMyPlace(uuid: String) {
        Observable.create<Unit> {
            myPlacesRepoImpl.addMyPlace(MyPlace(uuidString = uuid))
        }.flatMap { _ ->
            myPlacesRepoImpl.findByUuid(theUUId).toObservable()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).take(1)
            .autoDisposable(scopeProvider)
            .subscribe {
                theLateTime = it.time
                Log.d("jiplace create", "jiplace created from jiplace other")
            }
    }

    //after picking a date. we launch the timepicker
    //to get a more refined timestamp, we add the calendar date selected
    //to the time picked to create the MyPlace date field
    //the placeUUID is for the MyPlace object we're picking date for
    fun launchTimePicker(date: CalendarDate, placeUuid: String) {
        var theUUId = placeUuid
        var theBundle = Bundle()
        theBundle.apply {
            putString("theDate", "$date")
            putString("theUuid", theUUId)
        }
        Log.d("theDate", "$date")
        Log.d("theUuid", theUUId)
        TimePickerFragment().apply {
            setArguments(theBundle)
        }.show(supportFragmentManager, "timePicker")
        submitSelectedDate.isEnabled = true
        myPlacesRepoImpl.findByUuid(theUUId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .takeUntil { theLateTime != null && it.time != theLateTime }
            .autoDisposable(scopeProvider)
            .subscribe {
                if (it.time != null) {
                    selectedDateTv.text = "selected date is ${it.time}"
                }
            }
    }

    fun launchPlacePickerWorker(latitude: Double, longitude: Double, placeUUD: String) {
        var placePickWorker =
            OneTimeWorkRequestBuilder<PlacePickerWorker>().addTag("place-picker").setInputData(
                Data.Builder()
                    .putString("UuidKey", theUUId)
                    .putAll(mapOf("latitude" to "$latitude", "longitude" to "$longitude"))
                    .build()
            )
                .build()
        Log.d("i went", "past place picker worker")
        WorkManager.getInstance().enqueue(placePickWorker)
    }

}
