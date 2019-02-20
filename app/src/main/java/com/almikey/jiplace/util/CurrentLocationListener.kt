package com.almikey.jiplace.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.lifecycle.LiveData
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult

class CurrentLocationListener(var appContext: Context) :LiveData<Location>(){
   var fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)
    var theClient = fusedLocationClient.asGoogleApiClient()

    fun getPerm(){
        var permissionStatus =
            ContextCompat.checkSelfPermission(appContext, "android.Manifest.permission.ACCESS_FINE_LOCATION")


        if(permissionStatus!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                appContext as Activity,
                arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            fusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback, null)
        }else
        {
            fusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback, null)
        }
    }


    fun createLocationRequest():LocationRequest {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return locationRequest!!
    }

     override fun onActive() {
        super.onActive()
        getPerm()
    }

    override fun onInactive() {
        super.onInactive()
        Log.d("client connect","code works")
        fusedLocationClient.removeLocationUpdates(mLocationCallback)
    }


   var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            for (location in locationResult.locations){
                value = location
            }
        }
}

}