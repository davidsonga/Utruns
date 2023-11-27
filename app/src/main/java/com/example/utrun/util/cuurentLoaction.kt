package com.example.utrun.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class cuurentLoaction {

    @SuppressLint("SuspiciousIndentation")
    fun setUserCurrentLocation(context:Context):Int {
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context) // Replace 'context' with the appropriate context
        var num:Int =1
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions
             return num
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val myLatitude = location.latitude
                    val myLongitude = location.longitude

                   val location= FirebaseDatabase.getInstance().reference.child("login").child("email").child(FirebaseAuth.getInstance().uid.toString())



                                        location.child("latitude").setValue(myLatitude)
                                        location.child("longitude").setValue(myLongitude)

                                             .addOnSuccessListener {
                                                 num=1
                                             }


                }
            }
 return num
    }
}