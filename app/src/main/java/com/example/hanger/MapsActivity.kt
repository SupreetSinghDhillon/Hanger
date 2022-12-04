package com.example.hanger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.hanger.databinding.ActivityMapsBinding
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager

    private var mapCentered = false
    private lateinit var  markerOptions: MarkerOptions

    private lateinit var selectedLocation: LatLng;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapClickListener(this)
        markerOptions = MarkerOptions()

        checkPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            locationManager.removeUpdates(this)
    }

    fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)
            if(provider != null) {
                val location = locationManager.getLastKnownLocation(provider)

                if (location != null)
                    onLocationChanged(location) // update initial camera
            }
        } catch (e: SecurityException) {
        }
    }


    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        selectedLocation = latLng
        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10f)
            mMap.animateCamera(cameraUpdate)
            mapCentered = true
        }
    }

    override fun onMapClick(latLng: LatLng) {
        mMap.clear() // clear initial selected marker
        markerOptions.position(latLng!!)
        mMap.addMarker(markerOptions)
        selectedLocation = latLng
    }


    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        else
            initLocationManager()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) initLocationManager()
        }
    }

    fun saveLocationOnClick (view: View) {
        var returnIntent: Intent = Intent();
        var bundle: Bundle = Bundle()
        bundle.putParcelable("selectedLocation", selectedLocation)
        returnIntent.putExtra("bundle", bundle)
        setResult(0, returnIntent)
        finish()
    }

    fun cancelMapOnClick (view: View) {
        finish()
    }
}