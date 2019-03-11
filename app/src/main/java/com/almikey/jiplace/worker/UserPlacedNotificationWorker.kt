package com.almikey.jiplace.worker

import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.repository.MyPlacesRepositoryImpl
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

class UserPlacedNotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params),
    KoinComponent {
    val myPlacesRepoImpl: MyPlacesRepositoryImpl by inject()
    val theLongitude = inputData.getString("theLongitude")?.toFloat()!!
    val theLatitude = inputData.getString("theLatitude")?.toFloat()!!
    val theTime = inputData.getString("theTime")?.toLong()!!

    override fun doWork(): Result {
        runBlocking {
            var myPlaces = myPlacesRepoImpl.findByLocationData(theLatitude, theLongitude, theTime)
                .subscribeOn(Schedulers.io()).blockingFirst()
            myPlaces.forEach {
                sendJiplaceNotification(it)
            }
        }
        return Result.success()
    }


    fun sendJiplaceNotification(myPlace: MyPlace) {

        // Prepare the arguments to pass in the notification
        val arguments = Bundle().apply {
            putString("theUUID", myPlace.uuidString)
            putDouble("latitude", myPlace.location.latitude.toDouble())
            putDouble("longitude", myPlace.location.longitude.toDouble())
            putLong("theTime", myPlace.time.time)
        }

// Prepare the pending intent, while specifying the graph and destination
        val pendingIndent = NavDeepLinkBuilder(applicationContext)
            .setGraph(R.navigation.nav_graph)
            .setDestination(R.id.myPlacesUserFragment)
            .setArguments(arguments)
            .createPendingIntent()

        val formatter = DateTimeFormat.forPattern("d MMMM YYYY")
        val theDate = formatter.print(DateTime(myPlace.time))

        val notificationBuilder = NotificationCompat.Builder(getApplicationContext(), "channel_id")
            .setContentTitle("A user placed themselves on $theDate")
            .setContentText(myPlace.hint)
            .setContentIntent(pendingIndent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle())
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)

        val notificationManager =
            getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notificationBuilder.build())
    }

}