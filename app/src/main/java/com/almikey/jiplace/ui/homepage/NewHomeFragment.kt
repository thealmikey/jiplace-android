package com.almikey.jiplace.ui.homepage


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

import com.almikey.jiplace.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task

import kotlinx.android.synthetic.main.fragment_new_home_jiplace.*

class NewHomeFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mLocationCallback: LocationCallback
    val REQUEST_CHECK_SETTINGS =5

    val useLocation =  {location:Location->
        textView4?.setText("latitude ${location.latitude} longitude ${location.longitude}")!!

    }


    fun createLocationRequest():LocationRequest {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return locationRequest!!
    }

    fun getCurrentLocationSettings(locationRequest: LocationRequest,context: Context): Task<Boolean>{
        var builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        var client:SettingsClient = LocationServices.getSettingsClient(context)
        var task:Task<Boolean> = client.checkLocationSettings(builder.build()).continueWith(RetryLocationTask())
        return task
    }

    fun onLocationSettingsChanged(task:Task<Boolean>,success:()->Unit,failed:()->Unit){
        task.addOnSuccessListener {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            success()

        }


       var hey =  task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    failed()

                    exception.startResolutionForResult(activity as Activity,REQUEST_CHECK_SETTINGS)
                    Log.d("yellow","bella")
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

     hey.addOnCompleteListener{
         success()
     }
    }



    class RetryLocationTask():Continuation<LocationSettingsResponse, Boolean>{
        override fun then(p0: Task<LocationSettingsResponse>): Boolean {
            var theRes = p0.getResult()?.locationSettingsStates?.isGpsUsable
            Log.d("i got","into the retry")
           return theRes?:false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Activity)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_home_jiplace, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        locButton.setOnClickListener{
            onLocationSettingsChanged(getCurrentLocationSettings(createLocationRequest(),activity as Activity),getJiplaceFn,isFailed)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val states:LocationSettingsStates = LocationSettingsStates.fromIntent(data)
        Log.d("i get","called with result")
        when(requestCode){
            REQUEST_CHECK_SETTINGS ->{
                when(resultCode){
                    Activity.RESULT_OK -> {
                        Log.d("yellow","bella")
//                        onLocationSettingsChanged(getCurrentLocationSettings(createLocationRequest(),activity as Activity),getJiplaceFn,isFailed)
//                        Log.d("i got location","in onActivityResult")
                    }
                    Activity.RESULT_CANCELED-> iCanceled()
                }
            }
        }
    }

        var getJiplaceFn:()->Unit ={
            getJiplacesLocation(useLocation)
        }

    var theTime = 0L
    var theDiff = 0L

    fun getDiffTime(old:Long,new:Long):Long{
        var result =0L
        if(old!=0L){
            if(new>old){
               return new - old

            }
        }
        return result
    }



    val getJiplacesLocation:((Location)->Unit)->Unit = {useLocation: (Location)->Unit ->
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
//                     Update UI with location data
//                     ...
                    if(theTime!=0L) {
                        var newDiff= getDiffTime(theTime,location.time)
                        if(newDiff>theDiff){
                            theDiff = newDiff
                            theTime=location.time
                        }
                    }
                    if(theTime==0L) {
                        theTime = location.time
                    }
                    if(theDiff!=0L) {
                       var theRes = (theDiff.toFloat()/1000).toFloat().toString()
                        timeBetween.setText("$theRes seconds")
                    }
//                    timeBetween.setText("${location.accuracy} seconds")
                    useLocation(location)
                }
            }
        }
        var permissionStatus = ContextCompat.checkSelfPermission(activity as Activity,"android.Manifest.permission.ACCESS_FINE_LOCATION")
        if(permissionStatus!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity as Activity, arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
                fusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback,null).addOnSuccessListener {
                    fusedLocationClient.removeLocationUpdates(mLocationCallback)
                }
        }else{
            fusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback,null).addOnSuccessListener {
                fusedLocationClient.removeLocationUpdates(mLocationCallback)
            }
        }
    }




    var iSucceed:()->Unit = {
        textView4.text="success"
    }
    var iCanceled:()->Unit = {
        textView4.text="i got cancelled b4 getting permission"
    }
    var isFailed:()->Unit = {
        textView4.text="failed...bt wait a bit n see :) \n" +
                "it's supposed to request locations \n n you cant click the button again too :|"
        locButton.isEnabled=false
    }

}
