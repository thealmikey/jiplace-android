package com.almikey.jiplace.ui.homepage

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.input
import kotlinx.android.synthetic.main.fragment_home_jiplace.*

import com.almikey.jiplace.R
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import android.app.Activity.RESULT_OK
import android.content.Intent
import com.google.android.gms.location.places.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task


class HomeFragment : Fragment() {


    private lateinit var locationManager: LocationManager
    val animator = ValueAnimator.ofFloat(0f, 1f).setDuration(4500)
    val animator2 = ValueAnimator.ofFloat(0f, 0.85f).setDuration(2400)


//    val locationListener = object : LocationListener {
//
//        override fun onLocationChanged(location: Location) {
//            animator.cancel()
//            locationTV?.setText("latitude ${location.latitude} longitude ${location.longitude}")
//        }
//
//        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
//        }
//
//        override fun onProviderEnabled(provider: String) {
//        }
//
//        override fun onProviderDisabled(provider: String) {
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        var theView = inflater.inflate(R.layout.fragment_home_jiplace, container, false)

        return theView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//
//        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
//        var theLayout = theLayout
//        var bgAnim = theLayout.background as AnimationDrawable
//        bgAnim.setEnterFadeDuration(2000)
//        bgAnim.setExitFadeDuration(4000)
//        bgAnim.start()
//
//        theChecked?.visibility = View.GONE
//        dismissButton.visibility = View.GONE
//        dismissButton.setOnClickListener{
//            theAnim.visibility = View.VISIBLE
//            theChecked.visibility = View.GONE
//            dismissButton.visibility = View.GONE
//        }
//
//        //ChatSDK.contact().
//                    var permissionStatus = ContextCompat.checkSelfPermission(activity as Activity,"android.Manifest.permission.ACCESS_FINE_LOCATION")
//            if(permissionStatus!= PackageManager.PERMISSION_GRANTED){
//        var mPlaceDetectionClient:PlaceDetectionClient = Places.getPlaceDetectionClient(activity as Activity, null)
//        theAnim.setOnClickListener {
//
//    }
//});
        }

//                ActivityCompat.requestPermissions(activity as Activity, arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0f,locationListener)
//            }
//            else{
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0f,locationListener)
//            }

//            animator2.addUpdateListener { animation -> theChecked.setProgress(animation.animatedValue as Float) }
//            animator.addUpdateListener { animation -> theAnim.setProgress(animation.animatedValue as Float) }
//            animator.doOnEnd {
//                theAnim.visibility = View.GONE
//                theChecked.visibility = View.VISIBLE
////                theChecked.playAnimation()
//
//                animator2.start()
//            }
//
//            animator2.doOnEnd {
//                dismissButton.visibility = View.VISIBLE
//                startDialogForHint()
//            }
//
//            animator.repeatCount = ValueAnimator.INFINITE
//            animator.repeatMode = ValueAnimator.REVERSE
//            animator.start()
//
//        }
//
//
//        some_place.setOnClickListener {
//
//            val builder = PlacePicker.IntentBuilder()
//
//            startActivityForResult(builder.build(activity as Activity), PLACE_PICKER_REQUEST)
//        }
//
//    }


//   override fun onActivityResult(requestCode:Int,resultCode:Int, data:Intent) {
//  if (requestCode == PLACE_PICKER_REQUEST) {
//    if (resultCode == RESULT_OK) {
//        var place: Place = PlacePicker.getPlace(data, activity as Activity);
//       var loc = place.latLng
//        locationTV?.setText("latitude ${loc.latitude} longitude ${loc.longitude}")
//    }
//  }
//}

    fun startDialogForHint(){
        lateinit var theRes:String
        var dialog = MaterialDialog(activity as Activity).show {
            customView(R.layout.jiplace_description_hint)
        }
        val customView = dialog.getCustomView()
        var theText = customView?.findViewById<EditText>(R.id.jiplaceDescription)
        theText?.text.toString()

        dialog.positiveButton {
            theRes = theText?.text.toString()
            Log.d("on save dialog",theRes)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(ViewPumpContextWrapper.wrap(context))
    }
}
