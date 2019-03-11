package com.almikey.jiplace.worker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.Room
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.almikey.jiplace.database.MyPlacesRoomDatabase
import com.almikey.jiplace.model.MyPlaceProfilePic
import com.almikey.jiplace.repository.MyPlacesRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import java.io.ByteArrayOutputStream

class UploadMyPlaceImageWorker(context: Context, params: WorkerParameters) : Worker(context, params), KoinComponent {
    val myPlacesDb = Room.databaseBuilder(applicationContext, MyPlacesRoomDatabase::class.java, "myplaces-db")
        .build()
    val myPlacePicDao = myPlacesDb.myPlacePicDao()

    val myPlacesRepoImpl: MyPlacesRepositoryImpl by inject()

    val uuidKey = inputData.getString("UuidKey")
    val theLocationMap = inputData.keyValueMap.get("location")
    var fifteenMinGroupUp = inputData.keyValueMap.get("fifteenMinGroupUp").toString()
    var fifteenMinGroupDown = inputData.keyValueMap.get("fifteenMinGroupDown").toString()
    var localPicUri = inputData.keyValueMap.get("localPicUri").toString()

    var firebaseAuth = FirebaseAuth.getInstance()
    var authStateListener: FirebaseAuth.AuthStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            var theFbId = firebaseUser.uid
            //
            var storageRef = FirebaseStorage.getInstance().getReference("$theFbId/images/$uuidKey")
            var ref: DatabaseReference = FirebaseDatabase
                .getInstance().reference

            var appContext = applicationContext
            var resolver = appContext.contentResolver

            val picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(localPicUri)))
            val baos = ByteArrayOutputStream()
            picture.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()


            storageRef.putBytes(data).addOnSuccessListener {

                it.metadata?.reference?.downloadUrl?.addOnSuccessListener { theFirebaseUrl ->
                    myPlacesRepoImpl.findByUuid(uuidKey!!).take(1)
                        .subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe {
                            var newPlace = it.copy(
                                profile = MyPlaceProfilePic(
                                    localPicUrl = it.profile.localPicUrl,
                                    firebasePicUrl = theFirebaseUrl.toString()
                                )
                            )
                            myPlacesRepoImpl.update(newPlace)
                        }
//                val childUpdates = HashMap<String, Any?>()
//                childUpdates["myplaceusers/$theFbId/profilepic/$fifteenMinGroupUp"] = theFirebaseUrl.toString()
//                childUpdates["myplaceusers/$theFbId/profilepic/$fifteenMinGroupDown"] = theFirebaseUrl.toString()
//                ref.updateChildren(childUpdates)
                    if (fifteenMinGroupUp == fifteenMinGroupDown) {
                        var refUp: DatabaseReference = ref.child("myplaceusers/$theFbId/profilepic/$fifteenMinGroupUp")
                        refUp.push().setValue(theFirebaseUrl.toString())
                    } else {
                        var refUp: DatabaseReference = ref.child("myplaceusers/$theFbId/profilepic/$fifteenMinGroupUp")
                        refUp.push().setValue(theFirebaseUrl.toString())
                        var refDown: DatabaseReference =
                            ref.child("myplaceusers/$theFbId/profilepic/$fifteenMinGroupDown")
                        refDown.push().setValue(theFirebaseUrl.toString())
                    }
                }
            }

        }
    }

    override fun doWork(): Result {
        try {
            runBlocking {
                firebaseAuth.addAuthStateListener(authStateListener)
            }
            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
    }
}