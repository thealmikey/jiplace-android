package com.almikey.jiplace.ui.homepage


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.*
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.core.session.NM
import co.chatsdk.core.types.AccountDetails
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.ui.activity.MyPlaceOtherCalendar
import com.almikey.jiplace.ui.my_places.places_list.MyPlaceViewModel
import com.almikey.jiplace.util.LocationUtil.locationSettingsObservable
import com.almikey.jiplace.util.ThreadCleanUp.deleteThreadsFromOtherSide
import com.almikey.jiplace.worker.HintPickerWorker
import com.almikey.jiplace.worker.MyLocationWorker
import com.almikey.jiplace.worker.MyPlacesFirebaseSyncWorker
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.operators.completable.CompletableFromAction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home_jiplace.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

class HomeFragment : Fragment() {


    val firebaseWorker by lazy {
        var constraint: Constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        OneTimeWorkRequestBuilder<MyPlacesFirebaseSyncWorker>().setConstraints(constraint).build()
    }


    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(this) }

    val firebaseAuth: FirebaseAuth by lazy<FirebaseAuth> { FirebaseAuth.getInstance() }

    var authenticating = false;

    fun anonymousLogin() {
        var details: AccountDetails = AccountDetails();
        details.type = AccountDetails.Type.Anonymous;
        authenticateWithDetails(details);
    }

    fun authenticateWithDetails(details: AccountDetails) {
        if (authenticating) {
            return
        }
        authenticating = true

        theProgressBar.visibility = View.VISIBLE
        jiPlaceNow.isEnabled = false
        jiPlaceOther.isEnabled = false
        ChatSDK.auth().authenticate(details)
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                authenticating = false
                theProgressBar.visibility = View.GONE
                jiPlaceNow.isEnabled = true
                jiPlaceOther.isEnabled = true
                NM.auth().authenticateWithCachedToken()
                ChatSDK.core().goOnline()
            }.autoDisposable(scopeProvider)
            .subscribe({
                Toast.makeText(activity, "i succeeded in making you an anon account", Toast.LENGTH_LONG)

            },
                { e ->
                    Toast.makeText(activity, "i fucked up", Toast.LENGTH_LONG)
                    ChatSDK.logError(e)
                })
    }


    var authStateListener: FirebaseAuth.AuthStateListener =
        FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                anonymousLogin()
            } else {
                NM.auth().authenticateWithCachedToken()
                ChatSDK.core().goOnline()

                deleteThreadsFromOtherSide(scopeProvider)
            }
        }

    val myPlacesViewModel: MyPlaceViewModel by viewModel()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    val REQUEST_CHECK_SETTINGS = 5


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_jiplace, container, false)
    }

    @SuppressLint("AutoDispose")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        theProgressBar.visibility = View.GONE

        jiPlaceOther.setOnClickListener {
            var intent = Intent(activity, MyPlaceOtherCalendar::class.java)
            startActivityForResult(intent, 2)
        }

        firebaseAuth.addAuthStateListener(authStateListener)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Activity)
        jiPlaceNow.setOnClickListener {
            var theUUId = UUID.randomUUID().toString()
            fun askForHint(): Unit {
                lateinit var theRes: String
                var dialog = MaterialDialog(activity as Activity).show {
                    customView(R.layout.jiplace_description_hint)
                }
                val customView = dialog.getCustomView()
                var theText = customView?.findViewById<EditText>(R.id.jiplaceDescription)
                theText?.text.toString()

                dialog.negativeButton {
                    CompletableFromAction {
                        myPlacesViewModel.addPlace(MyPlace(uuidString = theUUId))
                    }.subscribeOn(Schedulers.io())
                        .autoDisposable(scopeProvider)
                        .subscribe({
                            var locWorker = OneTimeWorkRequestBuilder<MyLocationWorker>().addTag("loc-rx").setInputData(
                                Data.Builder()
                                    .putString("UuidKey", theUUId).build()
                            )
                                .build()
                            WorkManager.getInstance().beginWith(locWorker).then(firebaseWorker).enqueue()
                        })
                }

                dialog.positiveButton {
                    theRes = theText?.text.toString()
                    CompletableFromAction {
                        myPlacesViewModel.addPlace(MyPlace(uuidString = theUUId, hint = theRes))
                    }.subscribeOn(Schedulers.io())
                        .autoDisposable(scopeProvider)
                        .subscribe({
                            var locWorker = OneTimeWorkRequestBuilder<MyLocationWorker>().addTag("loc-rx").setInputData(
                                Data.Builder()
                                    .putString("UuidKey", theUUId).build()
                            )
                                .build()
                            WorkManager.getInstance().beginWith(locWorker).then(firebaseWorker).enqueue()
                        }, { err -> Log.d("the error", "many of horror:${err.message}") })
                }

                return Unit
            }

            var permissionStatus =
                ContextCompat.checkSelfPermission(
                    activity as Activity,
                    "android.Manifest.permission.ACCESS_FINE_LOCATION"
                )
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                locationSettingsObservable(this.activity as Activity, REQUEST_CHECK_SETTINGS).subscribe {
                    if (it) {
                        askForHint()
                    }
                }
            } else if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    activity as Activity,
                    arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
                locationSettingsObservable(this.activity as Activity, REQUEST_CHECK_SETTINGS)
                    .subscribe {
                        if (it) {
                            askForHint()
                        }
                    }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK) {
            val theUuid = data?.getStringExtra("theUuid")
            getHintAfterJiplaceOther(theUuid!!)
        }
    }

    fun getHintAfterJiplaceOther(theUuid: String) {
        lateinit var theHintStr: String
        var dialog = MaterialDialog(activity as Activity).show {
            customView(R.layout.jiplace_description_hint)
        }
        val customView = dialog.getCustomView()
        var theText = customView?.findViewById<EditText>(R.id.jiplaceDescription)
        theText?.text.toString()

        dialog.negativeButton {
            theHintStr = "no text hint was given :("
            var hintPickWorker = OneTimeWorkRequestBuilder<HintPickerWorker>().addTag("hint-picker").setInputData(
                Data.Builder()
                    .putString("UuidKey", theUuid).putString("hint", theHintStr).build()
            )
                .build()
            Log.d("i went", "past hint picker worker")
            WorkManager.getInstance().beginWith(hintPickWorker).then(firebaseWorker).enqueue()
        }
        dialog.positiveButton {
            theHintStr = theText?.text.toString()
            var hintPickWorker = OneTimeWorkRequestBuilder<HintPickerWorker>().addTag("hint-picker").setInputData(
                Data.Builder()
                    .putString("UuidKey", theUuid).putString("hint", theHintStr).build()
            )
                .build()
            Log.d("i went", "past hint picker worker")
            WorkManager.getInstance().beginWith(hintPickWorker).then(firebaseWorker).enqueue()
        }
    }
}


