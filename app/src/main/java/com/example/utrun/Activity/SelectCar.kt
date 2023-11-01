package com.example.utrun.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.example.utrun.Fragment.Home
import com.example.utrun.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

class SelectCar : AppCompatActivity() {
    // Firebase reference (assuming you're using Firebase Realtime Database)
    private val databaseReference = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_car)

        val spinnerVehicles: Spinner = findViewById(R.id.spinner_vehicles)
        val continueBtn: Button = findViewById(R.id.continueBtn)

        fetchVehicleBrands { brandsList ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, brandsList)
            spinnerVehicles.adapter = adapter
        }


        continueBtn.setOnClickListener {


                    val selectedBrand = spinnerVehicles.selectedItem.toString()
                    val inputString = selectedBrand
                    val parts = inputString.split(",")
                    val lastValue = parts.last()
                    checkVehicleAvailabilityAndReserve(lastValue)



        }
    }

    private fun fetchVehicleBrands(callback: (List<String>) -> Unit) {
        databaseReference.child("vehicles").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val brandsList = mutableListOf<String>()


                for (vehicleSnapshot in snapshot.children) {
                    val brand = vehicleSnapshot.child("brand").getValue(String::class.java) ?: ""
                    val model = vehicleSnapshot.child("model").getValue(String::class.java) ?: ""
                    val plate = vehicleSnapshot.child("numberPlate").getValue(String::class.java) ?: ""
                    val isAvailable = vehicleSnapshot.child("isAvailable").getValue(Boolean::class.java) ?: true
                    if(isAvailable){
                        if (brand.isNotEmpty()) {
                            brandsList.add("${brand},${model},${plate}")

                        }
                    }

                }
                callback(brandsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SelectCar, "Failed to load vehicle brands", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkVehicleAvailabilityAndReserve(selectedBrand: String) {
        databaseReference.child("vehicles").orderByChild("numberPlate").equalTo(selectedBrand)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val Id = snapshot.key
                        val vehicleSnapshot = snapshot.children.first()
                        val isAvailable = vehicleSnapshot.child("isAvailable").getValue(Boolean::class.java) ?: true
                        val brand = vehicleSnapshot.child("brand").getValue(String::class.java) ?:""
                        val numberPlate = vehicleSnapshot.child("numberPlate").getValue(String::class.java) ?:""
                        if (isAvailable) {

                            Toast.makeText(this@SelectCar, "Vehicle selected successfully", Toast.LENGTH_SHORT).show()

                            vehicleSnapshot.ref.child("isAvailable").setValue(false)
                            vehicleSnapshot.ref.child("key").setValue(FirebaseAuth.getInstance().uid)




                        } else {
                            Toast.makeText(this@SelectCar, "Vehicle is already selected! Please choose another car.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SelectCar, "Selected vehicle not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SelectCar, "Failed to check vehicle availability", Toast.LENGTH_SHORT).show()
                }
            })
    }
}