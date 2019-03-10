package com.almikey.jiplace.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import android.R
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.almikey.jiplace.worker.UserPlacedNotificationWorker
import android.os.Vibrator
import com.almikey.jiplace.ui.call.AudioCallActivity


class MyPlaceFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String?) {
        Log.d("msg service", "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }

    fun sendRegistrationToServer(token: String?) {
        var firebaseAuth = FirebaseAuth.getInstance()
        var authStateListener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.uid != null) {
                var theFbId = firebaseAuth.uid
                var ref: DatabaseReference = FirebaseDatabase
                    .getInstance()
                    .getReference("myplaceusers/$theFbId/token");
                ref.setValue(token)
            }
        }
        firebaseAuth.addAuthStateListener(authStateListener)

    }


    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // Check if message contains a data payload.
        if (remoteMessage!!.data.size > 0 && remoteMessage.data.get("type").toString()=="user_placed") {
            Log.d("remote msg", "Message data payload: " + remoteMessage.data)

            var theTime:String = remoteMessage.data.get("time").toString();
            var theLatitude:String = remoteMessage.data.get("latitude").toString();
            var theLongitude:String = remoteMessage.data.get("longitude").toString();
                    var userNotificationWorker = OneTimeWorkRequestBuilder<UserPlacedNotificationWorker>().addTag("notification worker").
            setInputData(
                Data.Builder()
                    .putString("theTime", theTime).putString("theLatitude",theLatitude).putString("theLongitude", theLongitude).build()
            )
            .build()
            WorkManager.getInstance().enqueue(userNotificationWorker)

        }else if (remoteMessage!!.data.size > 0 && remoteMessage.data.get("type").toString()=="user_call") {
            //we get the caller ID and we can use it to get the other person's SDP stuff
            //and set it as remote
            var callerId:String =  remoteMessage.data.get("caller_id").toString();
            sendCallJiplaceNotification(callerId)
        }
        // Check if message contains a notification payload.
        if (remoteMessage.notification != null) {
            Log.d("remote msg", "Message Notification Body: " + remoteMessage.notification!!.body!!)
        }
//        val message = remoteMessage
//        sendJiplaceNotification(message)
    }



    fun sendCallJiplaceNotification(callerId: String) {
        val arguments = Bundle().apply {
            putBoolean("received", true)
            putString("other_user_to_call",callerId )
        }
        val answerIntent = Intent(this, AudioCallActivity::class.java).apply {
            putExtras(arguments)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, answerIntent, 0)
        val notificationBuilder = NotificationCompat.Builder(this, "channel_id")
            .setContentTitle("Call")
            .setContentText("call from Jiplace")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.BigTextStyle())
            .setSmallIcon(android.R.drawable.btn_star_big_on)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_CALL)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notificationBuilder.build())
    }




}