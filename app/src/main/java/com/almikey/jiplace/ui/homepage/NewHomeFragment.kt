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
import androidx.lifecycle.LiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
//import androidx.lifecycle.Observer
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

import com.almikey.jiplace.R
import com.almikey.jiplace.ui.my_places.MyPlacesViewModel
import com.almikey.jiplace.util.CurrentLocationListener
import com.almikey.jiplace.util.CurrentLocationRx
import com.almikey.jiplace.worker.MyLocationWorker
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

import kotlinx.android.synthetic.main.fragment_new_home_jiplace.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NewHomeFragment : Fragment() {



    val myActivity:Activity by lazy<Activity>{
        this.activity as Activity
    }

    val currLocRx:CurrentLocationRx by inject()

    val myPlacesViewModel: MyPlacesViewModel by viewModel()

        private lateinit var fusedLocationClient: FusedLocationProviderClient
        private lateinit var mLocationCallback: LocationCallback
        val REQUEST_CHECK_SETTINGS = 5


        fun startDialogForHint() {
            lateinit var theRes: String
            var dialog = MaterialDialog(activity as Activity).show {
                customView(R.layout.jiplace_description_hint)
            }
            val customView = dialog.getCustomView()
            var theText = customView?.findViewById<EditText>(R.id.jiplaceDescription)
            theText?.text.toString()

            dialog.positiveButton {
                theRes = theText?.text.toString()
                Log.d("on save dialog", theRes)
            }
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
            locButton.setOnClickListener {
                var permissionStatus =
                    ContextCompat.checkSelfPermission(activity as Activity, "android.Manifest.permission.ACCESS_FINE_LOCATION")
                if(permissionStatus!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        activity as Activity,
                        arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        1
                    )
                        var locWorker = OneTimeWorkRequestBuilder<MyLocationWorker>().addTag("loc-rx")
                            .build()
                    WorkManager.getInstance().enqueue(locWorker)
                }else{
                    var locWorker = OneTimeWorkRequestBuilder<MyLocationWorker>().addTag("loc-rx")
                        .build()
                    WorkManager.getInstance().enqueue(locWorker)
                }
            }
        }

 }


