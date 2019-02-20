package com.almikey.jiplace.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.almikey.jiplace.MainActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.*
import com.google.android.gms.location.*
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.tasks.Task
import io.reactivex.functions.Cancellable
import kotlinx.android.synthetic.main.fragment_new_home_jiplace.*

class CurrentLocationRx(var context: Context) {

    var fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    //we have to write code to react on LocationChanged

    fun createLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest.create()?.apply {
            interval = 1000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        return locationRequest!!
    }

    var observeLocation: Observable<Location> = Observable.create<Location> { emitter ->

        var mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (!emitter.isDisposed()) {
                        emitter.onNext(location);
                    }
                }
            }
        }

        emitter.setCancellable {
            object : Cancellable {
                override fun cancel() {
                    fusedLocationClient.removeLocationUpdates(mLocationCallback)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(createLocationRequest(), mLocationCallback, null)
    }
}