package com.example.utrun.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.utrun.R
import com.example.utrun.util.progressDialog
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class Pick_Drop : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private var locationManager: LocationManager? = null
    private val obj: progressDialog = progressDialog()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_pick__drop, container, false)

        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager


        return view
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()

        requestLocationPermission()



    }


    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()



    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap!!.uiSettings.isZoomControlsEnabled=true
        googleMap!!.uiSettings.isZoomGesturesEnabled=true
        googleMap!!.uiSettings.isScrollGesturesEnabled=true

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap!!.isMyLocationEnabled = true
        } else {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        // Enable user location tracking
        if (checkLocationPermission()) {
            val sharedPref: SharedPreferences =requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)

            val companyName = sharedPref.getString("organization", null)
            var latitude:Double=0.0
            var longitude:Double=0.0
            if(companyName != null){
                latitude= sharedPref.getString("lat", null).toString().toDouble()
                longitude = sharedPref.getString("long", null).toString().toDouble()
            }


            // Ensure that the map is ready and there's valid location data
            if (googleMap != null && latitude != 0.0 && longitude != 0.0) {
                setLocationOnMap(LatLng(latitude, longitude), companyName)
                val editor = sharedPref.edit()

                editor.remove("lat")
                editor.remove("long")
                editor.remove("organization")

                editor.apply()
            }
        }



        }
    private fun setLocationOnMap(location: LatLng, name: String?) {
        val markerOptions = MarkerOptions()
            .position(location)
            .title( name)

        googleMap?.addMarker(markerOptions)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        obj.isProgressDialogDisable()
    }
    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        ) {
            // Show an explanation to the user and request permission
            // You can show a dialog explaining why you need location permission
            // and request permission when the user agrees.
        } else {
            // No explanation needed, we can request the permission.
            requestPermissions(permissions, Home.LOCATION_PERMISSION_REQUEST_CODE)
        }
    }


}