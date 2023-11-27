package com.example.utrun.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.utrun.R
import com.example.utrun.Service.AppLifecycleCallback
import com.example.utrun.Service.AppStateService
import com.example.utrun.models.Locations
import com.example.utrun.util.cuurentLoaction
import com.example.utrun.util.progressDialog
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
    private val obj: progressDialog = progressDialog()
    private lateinit var appLifecycleCallback: AppLifecycleCallback

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
        if (::mapView.isInitialized) {
            mapView.onDestroy()
        }
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::mapView.isInitialized) {
            mapView.onSaveInstanceState(outState)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap!!.uiSettings.isZoomControlsEnabled=true
        googleMap!!.uiSettings.isZoomGesturesEnabled=true
        googleMap!!.uiSettings.isScrollGesturesEnabled=true
        obj.isProgressDialogEnable(requireActivity(),"Please wait...")
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            googleMap!!.isMyLocationEnabled = true
        } else {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        // Enable user location tracking
        if (checkLocationPermission()) {
            if (isAdded && context != null) {


            val getLocation = FirebaseDatabase.getInstance().reference.child("login").child("email")

            var num:Int =0

                getLocation.addValueEventListener(object : ValueEventListener {
                    @SuppressLint("CommitPrefEdits")
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (!isAdded || activity == null) {
                            return
                        }

                        for(locationSnapshop in snapshot.children){
                            val uid = locationSnapshop.key.toString()
                            val lat = locationSnapshop.child("latitude").getValue(Double::class.java)?:0.0
                            val long = locationSnapshop.child("longitude").getValue(Double::class.java)?:0.0
                            val picture= locationSnapshop.child("Picture").getValue(String::class.java)?:""
                            val Name= locationSnapshop.child("name").getValue(String::class.java)?:""
                            val surname= locationSnapshop.child("surname").getValue(String::class.java)?:""
                            val fullName= "$Name $surname"


                            if(picture !=""){
                                if(lat !=0.0 && long !=0.0  ){

                                    val add = Locations(lat,long,fullName,picture,uid)
                                    num++
                                    array.add(add)
                                }
                            }




                        }

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
                                if(!location.picture.equals("")  ){
                                    getLocation(location.latitude!!,location.longitude!!,decodeStringImage(location.picture!!),location.fullName!!,location.UID!!)

                                }

                            }
                            obj.isProgressDialogDisable()
                        }



                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            } else {
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Internet connection not stable", Toast.LENGTH_LONG).show()
                }
            }


        }else{
            Toast.makeText(requireContext(),"Error occur, Please restart the app",Toast.LENGTH_LONG).show()
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

        if (!isAdded || activity == null) {
            return
        }

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
        val obj:cuurentLoaction = cuurentLoaction()
        obj.setUserCurrentLocation(requireContext())

    }

    private fun setLocationOnMap(location: LatLng, name: String?) {
        if (googleMap != null && isAdded && context != null) {
            val markerOptions = MarkerOptions()
                .position(location)
                .title(name)

            googleMap?.addMarker(markerOptions)
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            obj.isProgressDialogDisable()
        }
    }
}