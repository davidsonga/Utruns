package com.example.utrun.util

import android.Manifest
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

                    FirebaseDatabase.getInstance().reference.child("login").child("email")
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                for (loginSnapshot in snapshot.children) {
                                    val UID = loginSnapshot.key
                                    val picture =
                                        loginSnapshot.child("Picture").getValue(String::class.java)
                                    val name =
                                        loginSnapshot.child("name").getValue(String::class.java)
                                    val surname =
                                        loginSnapshot.child("surname").getValue(String::class.java)
                                    val fullName = "$name $surname"
                                    if (UID == FirebaseAuth.getInstance().uid) {
                                        val setLocation =
                                            FirebaseDatabase.getInstance().reference.child("currentLocation")
                                                .child(FirebaseAuth.getInstance().uid.toString())
                                        val nameSurnameMap = hashMapOf(
                                            "fullName" to fullName,
                                            "Picture" to picture,
                                            "latitude" to myLatitude,
                                            "longitude" to myLongitude)
                                        //setLocation.child("latitude").setValue(myLatitude)
                                        //setLocation.child("longitude").setValue(myLongitude)
                                        setLocation.setValue(nameSurnameMap)


                                     //   setLocation.child("Picture").setValue(picture)
                                     //   setLocation.child("fullName").setValue(fullName)
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle onCancelled event
                            }
                        })
                }
            }
 return num
    }
}