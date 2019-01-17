package com.almikey.jiplace.ui.homepage


import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.repository.MyPlacesRepository
import com.almikey.jiplace.ui.my_places.MyPlacesViewModel
import com.almikey.jiplace.worker.MyLocationWorker
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_new_home_jiplace.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class NewHomeFragment : Fragment() {


    val myPlacesViewModel: MyPlacesViewModel by viewModel()

        val myPlacesRepo: MyPlacesRepository by inject()
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private lateinit var mLocationCallback: LocationCallback
        val REQUEST_CHECK_SETTINGS = 5


        fun startDialogForHint() {

        }


        fun createLocationRequest(): LocationRequest {
            val locationRequest = LocationRequest.create()?.apply {
                interval = 1000
                fastestInterval = 500
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            return locationRequest!!
        }

        fun getCurrentLocationSettings(locationRequest: LocationRequest, context: Context): Task<LocationSettingsResponse> {
            var builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
            var client: SettingsClient = LocationServices.getSettingsClient(context)
            var task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
            return task
        }


        fun locationSettingsObservable():Observable<Boolean>{
            return Observable.create<Boolean> {emitter ->
          var theTask =  getCurrentLocationSettings(createLocationRequest(),activity as Activity)
            theTask.addOnSuccessListener {
                    emitter.onNext(true)
                emitter.onComplete()
            }
           var theFail = theTask.addOnFailureListener { exception ->
                if (exception is ResolvableApiException){
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        emitter.onNext(false)

                        exception.startResolutionForResult(activity as Activity,REQUEST_CHECK_SETTINGS)
                        Log.d("yellow","bella")
                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
                theFail.addOnCompleteListener {
                    emitter.onNext(true)
                    emitter.onComplete()
                }
        }
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            super.onCreateView(inflater, container, savedInstanceState)
            // Inflate the layout for this fragment
            return inflater.inflate(R.layout.fragment_new_home_jiplace, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Activity)
            jiPlaceNow.setOnClickListener {
                var theUUId = UUID.randomUUID().toString()
                fun askForHint():Unit{
                    lateinit var theRes: String
                    var dialog = MaterialDialog(activity as Activity).show {
                        customView(R.layout.jiplace_description_hint)
                    }
                    val customView = dialog.getCustomView()
                    var theText = customView?.findViewById<EditText>(R.id.jiplaceDescription)
                    theText?.text.toString()

                    dialog.negativeButton {
                        CompletableFromAction{
                            myPlacesViewModel.addPlace(MyPlace(uuidString = theUUId))
                        }.subscribeOn(Schedulers.io()).subscribe({
                            var locWorker = OneTimeWorkRequestBuilder<MyLocationWorker>().addTag("loc-rx").
                                setInputData(
                                    Data.Builder()
                                        .putString("UuidKey", theUUId).build()
                                )
                                .build()
                            WorkManager.getInstance().enqueue(locWorker)
                        },{err->Log.d("the error","many of horror:${err.message}")})
                    }

                    dialog.positiveButton {
                        theRes = theText?.text.toString()
                        CompletableFromAction{
                            myPlacesViewModel.addPlace(MyPlace(uuidString = theUUId,hint = theRes))
                      }.subscribeOn(Schedulers.io()).subscribe ({
                            var locWorker = OneTimeWorkRequestBuilder<MyLocationWorker>().addTag("loc-rx").
                                setInputData(
                                    Data.Builder()
                                        .putString("UuidKey", theUUId).build()
                                )
                                .build()
                            WorkManager.getInstance().enqueue(locWorker)
                        },{err->Log.d("the error","many of horror:${err.message}")})
                    }

                    return Unit
                }
                var permissionStatus =
                    ContextCompat.checkSelfPermission(activity as Activity, "android.Manifest.permission.ACCESS_FINE_LOCATION")
                if(permissionStatus == PackageManager.PERMISSION_GRANTED) {
                locationSettingsObservable().subscribe{
                    if(it){
                        askForHint()
                    }
                }
            }
                else if(permissionStatus!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        activity as Activity,
                        arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                    locationSettingsObservable().subscribe{
                        if(it){
                            askForHint()
                        }
                    }

                }
            }
        }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }
 }


