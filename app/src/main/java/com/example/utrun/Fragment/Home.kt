package com.example.utrun.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.location.LocationManager
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.utrun.R
import com.example.utrun.models.Locations
import com.example.utrun.util.cuurentLoaction
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class Home : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private var locationManager: LocationManager? = null
    private var myLatitude: Double = 0.0
    private var myLongitude: Double = 0.0
    private var array : MutableList<Locations> = mutableListOf()
    private var string:String =""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize the MapView
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager


        return view
    }
    @SuppressLint("MissingPermission")
    fun setUserCurrentLocation( ){
        val location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if (location != null) {
            myLatitude = location.latitude
            myLongitude = location.longitude

        }
        FirebaseDatabase.getInstance().reference.child("login").child("email").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for(loginSnapshot in snapshot.children){
                    val UID = loginSnapshot.key
                    val picture = loginSnapshot.child("Picture").getValue(String::class.java)
                    val name = loginSnapshot.child("name").getValue(String::class.java)
                    val surname = loginSnapshot.child("surname").getValue(String::class.java)
                    val fullName = "${name} ${surname}"
                    if(UID == FirebaseAuth.getInstance().uid){

                        val setLocation= FirebaseDatabase.getInstance().reference.child("currentLocation").child(FirebaseAuth.getInstance().uid.toString())

                        setLocation.child("latitude").setValue(myLatitude)
                        setLocation.child("longitude").setValue(myLongitude)
                        setLocation.child("Picture").setValue(picture)
                        setLocation.child("fullName").setValue(fullName)
                    }

                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

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

            val getLocation = FirebaseDatabase.getInstance().reference.child("currentLocation")

            getLocation.addValueEventListener(object : ValueEventListener {
                @SuppressLint("CommitPrefEdits")
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(locationSnapshop in snapshot.children){
                        val uid = locationSnapshop.key
                        val lat = locationSnapshop.child("latitude").getValue(Double::class.java)
                        val long = locationSnapshop.child("longitude").getValue(Double::class.java)
                        val picture= locationSnapshop.child("Picture").getValue(String::class.java)
                        val fullName= locationSnapshop.child("fullName").getValue(String::class.java)
                        val login=  FirebaseDatabase.getInstance().reference.child("login")


                        val add = Locations(lat,long,fullName,picture,uid)
                        array.add(add)
                    }
// Retrieve the location and organization information from the intent
                    //val latitude = requireActivity().intent.getDoubleExtra("lat", 0.0)
                    //   val longitude = requireActivity().intent.getDoubleExtra("long", 0.0)
                    // val companyName = requireActivity().intent.getStringExtra("organization")
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
                    }else{
                        for (location in array) {
                            getLocation(location.latitude!!,location.longitude!!,decodeStringImage(location.picture!!),location.fullName!!,location.UID!!)

                        }
                    }



                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        }
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
            requestPermissions(permissions, LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun getLocation(lat: Double, lon: Double, picture: Bitmap?, fullname:String,uid:String) {
        try {
            val markerOptions:MarkerOptions
            val userLocation:LatLng
            val location = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                myLatitude = location.latitude
                myLongitude = location.longitude


                if(uid == FirebaseAuth.getInstance().uid &&picture !=null){
                    userLocation = LatLng(myLatitude, myLongitude)
                    markerOptions = MarkerOptions()
                        .position(userLocation)
                        .title("Name: ${fullname}")
                }else{
                    userLocation = LatLng(lat, lon)
                    markerOptions = MarkerOptions()
                        .position(userLocation)
                        .title("Name: ${fullname}")
                }


                if (picture != null) {
                    // Create a circular and small version of the picture
                    val circularIcon = createCircularIcon(picture, 100) // Adjust 50 to your desired size

                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(circularIcon))
                }

                googleMap?.addMarker(markerOptions)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }


    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    fun decodeStringImage(stringImage: String): Bitmap? {
        try {
            val byteArray = Base64.decode(stringImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun createCircularIcon(bitmap: Bitmap, size: Int): Bitmap {
        // Create a circular Bitmap icon
        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, size, size)
        val rectF = RectF(rect)

        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = Color.RED // Set the background color to transparent if needed
        canvas.drawCircle(size.toFloat() / 2, size.toFloat() / 2, size.toFloat() / 2, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, null, rect, paint)

        return output
    }

    override fun onStart() {
        super.onStart()

    }

    private fun setLocationOnMap(location: LatLng, name: String?) {
        val markerOptions = MarkerOptions()
            .position(location)
            .title( name)

        googleMap?.addMarker(markerOptions)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }



}