package com.almikey.jiplace.ui.map

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.almikey.jiplace.R
import com.google.android.gms.common.api.Status

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.*
import android.widget.Toast
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import co.chatsdk.core.types.AccountDetails.token
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.activity_jiplace_maps.*


class MyPlaceMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var selectedPlace:Place
    private lateinit var autocompleteFragment:AutocompleteSupportFragment
    lateinit var placesClient: PlacesClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jiplace_maps)

        selectJiplaceButton.setOnClickListener {
            var placeBundle: Bundle = Bundle();
            placeBundle.putDouble("latitude",selectedPlace.latLng!!.latitude);
            placeBundle.putDouble("longitude",selectedPlace.latLng!!.longitude);
            placeBundle.putString("placeName",selectedPlace.name);
            var placeResultIntent: Intent = Intent().putExtras(placeBundle)
            setResult(Activity.RESULT_OK, placeResultIntent)
            finish()
        }


        placesClient = Places.createClient(this);
        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment

        autocompleteFragment.setCountry("KE")

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)



// Specify the types of place data to return.
        autocompleteFragment!!.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME))

    }




    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker where the user has typed in the
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        lateinit var thePlace:LatLng
// Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onError(p0: Status) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onPlaceSelected(place: Place) {
                selectJiplaceButton.isEnabled=true
                // TODO: Get info about the selected place.
                Log.i("jiplace map", "Place: " + place.getName() + ", " )
                thePlace = place.latLng!!
                selectedPlace = place
                theSelectedJiplace.text = selectedPlace.name
                // Add a marker in Sydney and move the camera
                mMap.addMarker(MarkerOptions().position(thePlace).title("Jiplace here"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(thePlace,18f))
            }

        })


        mMap.setOnMapClickListener(GoogleMap.OnMapClickListener { point ->
            // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
            // and once again when the user makes a selection (for example when calling fetchPlace()).
            var bounds = RectangularBounds.newInstance(point,point)
            autocompleteFragment.setLocationBias(bounds)
        })

    }
}
