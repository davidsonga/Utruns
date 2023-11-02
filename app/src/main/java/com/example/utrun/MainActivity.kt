package com.example.utrun

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.utrun.Activity.SelectCar
import com.example.utrun.Fragment.Home.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.example.utrun.Network.LoginProgress
import com.example.utrun.Service.AppLifecycleCallback
import com.example.utrun.Service.AppStateService
import com.example.utrun.Service.MyApp
import com.example.utrun.Service.isDeviceConnectToInternet
import com.example.utrun.util.cuurentLoaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity   :AppCompatActivity() {
    private var selectedUserRole: String = ""
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var btn_logIn: Button
    private var isTrue: Boolean = false
    private var objLogin: LoginProgress = LoginProgress()
    private lateinit var auth: FirebaseAuth
    private lateinit var appLifecycleCallback: AppLifecycleCallback
    private var refreshedToken: String = ""
    private lateinit var ln: LinearLayout
    private lateinit var rl: RelativeLayout
    private lateinit var btnInternetRefresh: Button
    private var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Location permissions have already been granted, you can proceed with location-related functionality.
        }

        ln = findViewById<LinearLayout>(R.id.ln)
        rl = findViewById<RelativeLayout>(R.id.rl)
        //declaring obj variable
        emailEt = findViewById(R.id.emailEt)
        passwordEt = findViewById(R.id.passwordEt)
        btn_logIn = findViewById(R.id.btn_logIn)
        //calling the life cycle of the app

        btnInternetRefresh = findViewById(R.id.btnInternetRefresh)
        btnInternetRefresh.setOnClickListener() {
            rl.visibility = View.GONE;
            onStart()
        }
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    refreshedToken = task.result

                }
            }

        //declaring and adding values to the spinner
        val userRoles = arrayOf("Select role", "Admin", "User")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userRoles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        //getting selected role from spinner


        // Inside your btn_logIn.setOnClickListener()
        btn_logIn.setOnClickListener() {

            isTrue = !emailEt.text.isEmpty()
                    && !passwordEt.text.isEmpty() && passwordEt.text.length >= 6 // Changed to >= 6 for a minimum length of 6 characters


            if (emailEt.text.isEmpty()) {
                emailEt.setError("Email field cannot be empty!!!")
            }

            if (passwordEt.text.isEmpty()) {
                passwordEt.setError("Password field cannot be empty!!!")
            }

            if (passwordEt.text.length < 6) { // Check for a minimum password length
                passwordEt.setError("Password length should be at least 6 characters")
            }

            if (isTrue) {

                //sending users details to the database to checkup
                objLogin.isLoginUser(
                    this,
                    emailEt.text.toString(),
                    passwordEt.text.toString(),
                    selectedUserRole,
                    refreshedToken
                )


            }
        }
    }


    //do not remove this

    override fun onStart() {
        super.onStart()

        val con: isDeviceConnectToInternet = isDeviceConnectToInternet()
        val mAuth = FirebaseAuth.getInstance().currentUser
        ln = findViewById<LinearLayout>(R.id.ln)
        rl = findViewById<RelativeLayout>(R.id.rl)

        ln.visibility = View.GONE
        if (con.isInternetConnected(this)) {
            if (mAuth != null) {
                val obj : cuurentLoaction = cuurentLoaction()
                obj.setUserCurrentLocation(this)
                appLifecycleCallback = (application as MyApp).appLifecycleCallback
                decisionMking()


            } else {
                ln.visibility = View.VISIBLE
            }
        } else {
            rl.visibility = View.VISIBLE
            btnInternetRefresh.visibility = View.VISIBLE
        }

    }


    //do not remove/modify this please
    override fun onDestroy() {
        super.onDestroy()

        if (!appLifecycleCallback.isAppInForeground()) {
            // Stop the AppStateService explicitly when the app is closed
            val serviceIntent = Intent(this, AppStateService::class.java)
            stopService(serviceIntent)
        } //else {
        // App is in the foreground, show a toast


    }

    private fun decisionMking() {
        var userHasCar: Boolean = false;
        FirebaseDatabase.getInstance().reference.child("vehicles")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (vehicleSnapshot in snapshot.children) {
                        val vehicleKey = vehicleSnapshot.key
                        val isAvailable: Boolean = vehicleSnapshot.child("isAvailable")
                            .getValue(Boolean::class.java) == true
                        val employeeUID = vehicleSnapshot.child("key").getValue(String::class.java)
                        if (!isAvailable && employeeUID == FirebaseAuth.getInstance().uid) {
                            val intent: Intent = Intent(this@MainActivity, HomePage::class.java)
                            startActivity(intent)

                            userHasCar = true
                        }


                    }
                    if (!userHasCar) {

                        val intent: Intent = Intent(this@MainActivity, SelectCar::class.java)
                        startActivity(intent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


    private fun requestLocationPermission(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            // Request permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
        return true
    }



}





