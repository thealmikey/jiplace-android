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
import androidx.fragment.app.FragmentTransaction
import androidx.work.*
import co.chatsdk.core.session.ChatSDK
import co.chatsdk.core.session.NM
import co.chatsdk.core.types.AccountDetails
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView

import com.almikey.jiplace.R
import com.almikey.jiplace.model.MyPlace
import com.almikey.jiplace.ui.calendar.MyPlaceCalendarActivity
import com.almikey.jiplace.ui.my_places.places_list.MyPlaceViewModel
import com.almikey.jiplace.ui.my_places.places_list.MyPlacesFragment
import com.almikey.jiplace.ui.my_places.places_list.MyPlacesFragmentOnHome
import com.almikey.jiplace.util.LocationUtil.locationSettingsObservable
import com.almikey.jiplace.util.ThreadCleanUp.deleteThreadsFromOtherSide
import com.almikey.jiplace.worker.HintPickerWorker
import com.almikey.jiplace.worker.MyLocationWorker
import com.almikey.jiplace.worker.MyPlacesServerSyncWorker
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_home_jiplace.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.UUID

class HomeFragment : Fragment() {


    val firebaseWorker by lazy {
        var constraint: Constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        OneTimeWorkRequestBuilder<MyPlacesServerSyncWorker>().setConstraints(constraint).build()
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
            var MyPlaceCalendar = Intent(activity, MyPlaceCalendarActivity::class.java)
            startActivityForResult(MyPlaceCalendar, 2)
        }

        firebaseAuth.addAuthStateListener(authStateListener)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity as Activity)
        jiPlaceNow.setOnClickListener {
            //turn off button on first click to stop multiple activations by multiple clicks
            jiPlaceNow.isEnabled = false
            var theUUId = UUID.randomUUID().toString()
            fun askForHint(): Unit {
                lateinit var hintText: String
                var dialog = MaterialDialog(activity as Activity).show {
                    customView(R.layout.jiplace_description_hint)
                }
                val customView = dialog.getCustomView()
                var theText = customView?.findViewById<EditText>(R.id.jiplaceDescription)
                theText?.text.toString()

                dialog.negativeButton {
                    savePlaceNowWithoutHintText(theUUId)
                    //turn button back on
                    jiPlaceNow.isEnabled = true
                }

                dialog.positiveButton {
                    hintText = theText?.text.toString()
                    savePlaceNowWithHintText(theUUId, hintText)
                    jiPlaceNow.isEnabled = true
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

        var ft: FragmentTransaction = getChildFragmentManager().beginTransaction();
        var myPlacesFrag = MyPlacesFragmentOnHome();
        ft.replace(R.id.myPlacesFragmentNested, myPlacesFrag);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == RESULT_OK) {
            val theUuid = data?.getStringExtra("theUuid")
            promptForHintAfterJiplaceOther(theUuid!!)
        }
    }

    fun promptForHintAfterJiplaceOther(theUuid: String) {
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

    fun savePlaceNowWithoutHintText(placeUuid: String) {
        myPlacesViewModel.addPlace(MyPlace(uuidString = placeUuid))
            .subscribeOn(Schedulers.io()).autoDisposable(scopeProvider)
            .subscribe({
                var locWorker = OneTimeWorkRequestBuilder<MyLocationWorker>().addTag("loc-rx").setInputData(
                    Data.Builder()
                        .putString("UuidKey", placeUuid).build()
                )
                    .build()
                WorkManager.getInstance().beginWith(locWorker).then(firebaseWorker).enqueue()
                Log.d("save place now", "was able to save placenow without hint,worker started")
            }, {
                Log.d("save place now", "was unable to save placenow without hint, err message is ${it.message}")
            })
    }

    fun savePlaceNowWithHintText(placeUuid: String, hintText: String) {
        myPlacesViewModel.addPlace(MyPlace(uuidString = placeUuid, hint = hintText))
            .subscribeOn(Schedulers.io())
            .autoDisposable(scopeProvider).subscribe({
                var locWorker = OneTimeWorkRequestBuilder<MyLocationWorker>().addTag("loc-rx").setInputData(
                    Data.Builder()
                        .putString("UuidKey", placeUuid).build()
                )
                    .build()
                WorkManager.getInstance().beginWith(locWorker).then(firebaseWorker).enqueue()
                Log.d("save place now", "was able to save placenow with hint,worker started")
            }, {
                Log.d("save place now", "was unable to save placenow with hint,err message is ${it.message}}")
            })
    }
}


