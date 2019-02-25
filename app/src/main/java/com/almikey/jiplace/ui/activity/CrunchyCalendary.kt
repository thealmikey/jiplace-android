package com.almikey.jiplace.ui.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.repository.MyPlacesRepository
import com.almikey.jiplace.util.TimePickerFragment
import com.almikey.jiplace.worker.PlacePickerWorker
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import com.uber.autodispose.lifecycle.autoDisposable
import io.reactivex.Observable

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_crunchy_calendary.*
import ru.cleverpumpkin.calendar.CalendarDate
import ru.cleverpumpkin.calendar.CalendarView
import java.text.SimpleDateFormat
import org.koin.android.ext.android.inject

import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class CrunchyCalendary : AppCompatActivity() {


    private val scopeProvider by lazy {
        AndroidLifecycleScopeProvider.from(this)
    }


    lateinit var theMainDate: Date;
    val myPlacesRepo: MyPlacesRepository by inject()
    var theUUId = UUID.randomUUID().toString()
    var theLateTime: Date? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crunchy_calendary)

        Observable.create<Unit> {
            myPlacesRepo.addMyPlace(MyPlace(uuidString = theUUId))
        }.flatMap { _ ->
            myPlacesRepo.findByUuid(theUUId).toObservable()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).take(1)
            .autoDisposable(scopeProvider)
            .subscribe {
                var theLateTime = it.time
                Log.d("jiplace create", "jiplace created from jiplace other")
            }

        var theCal = calendarDateView
        theCal.setupCalendar(selectionMode = CalendarView.SelectionMode.SINGLE)
        @SuppressWarnings
        theCal.onDateClickListener = { date ->
            // selectedDateTv.text = "the date is selected is $date"
            theMainDate = date.date
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
            myPlacesRepo.findByUuid(theUUId)
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


        submitSelectedDate.setOnClickListener {
            submitSelectedDate.isEnabled = false
            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(),"AIzaSyDW8L00sRZazeA-3IszCZr70scdmmsc9Ew");
            }
            // Set the fields to specify which types of place data to return.
            var fields:List<Place.Field> = Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME);

            // Start the autocomplete intent.
            var intent:Intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this);
            startActivityForResult(intent, 5);

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 5) {
            if (resultCode == RESULT_OK) {
                var place: Place = Autocomplete.getPlaceFromIntent(data!!);
                var loc = place.latLng
                var lat = loc!!.latitude.toFloat().toString()
                var lon = loc!!.longitude.toFloat().toString()
                var placePickWorker =
                    OneTimeWorkRequestBuilder<PlacePickerWorker>().addTag("place-picker").setInputData(
                        Data.Builder()
                            .putString("UuidKey", theUUId)
                            .putAll(mapOf("latitude" to "$lat", "longitude" to "$lon"))
                            .build()
                    )
                        .build()
                Log.d("i went", "past place picker worker")
                WorkManager.getInstance().enqueue(placePickWorker)
                val resultIntent = Intent()
                resultIntent.putExtra("theUuid", "$theUUId")
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
        }
    }

}
