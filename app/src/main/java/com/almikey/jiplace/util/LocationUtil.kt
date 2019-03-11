package com.almikey.jiplace.util

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import io.reactivex.Observable

object LocationUtil {
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


    fun locationSettingsObservable(activity:Activity,resultId:Int): Observable<Boolean> {
        return Observable.create<Boolean> { emitter ->
            var theTask = getCurrentLocationSettings(createLocationRequest(), activity as Activity)
            theTask.addOnSuccessListener {
                emitter.onNext(true)
                emitter.onComplete()
            }
            var theFail = theTask.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        emitter.onNext(false)

                        exception.startResolutionForResult(activity as Activity, resultId)
                        Log.d("yellow", "bella")
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
}