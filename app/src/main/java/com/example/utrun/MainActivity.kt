package com.example.utrun

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.utrun.Network.LoginProgress
import com.example.utrun.Service.AppLifecycleCallback
import com.example.utrun.Service.AppStateService
import com.example.utrun.Service.MyApp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity   :AppCompatActivity()  {
private var selectedUserRole: String = ""
    private lateinit var emailEt:EditText
    private lateinit var passwordEt:EditText
    private lateinit var btn_logIn:Button
    private var isTrue:Boolean =false
    private var objLogin:LoginProgress = LoginProgress()
    private lateinit var auth: FirebaseAuth
    private lateinit var appLifecycleCallback: AppLifecycleCallback
    private var refreshedToken:String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)





        //declaring obj variable
        emailEt = findViewById(R.id.emailEt)
        passwordEt = findViewById(R.id.passwordEt)
        btn_logIn = findViewById(R.id.btn_logIn)
        //calling the life cycle of the app


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
        val spinner = findViewById<Spinner>(R.id.spinnerUserRole)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedUserRole = userRoles[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Handle when nothing is selected (optional)
            }


        }

        // Inside your btn_logIn.setOnClickListener()
        btn_logIn.setOnClickListener() {

            isTrue = !selectedUserRole.equals("Select role") && !emailEt.text.isEmpty()
                    && !passwordEt.text.isEmpty() && passwordEt.text.length >= 6 // Changed to >= 6 for a minimum length of 6 characters

            if (selectedUserRole.equals("Select role") || selectedUserRole.isEmpty()) {
                Toast.makeText(this, "Please select a role", Toast.LENGTH_LONG).show()
            }

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
                objLogin.isLoginUser(this,emailEt.text.toString(),passwordEt.text.toString(),selectedUserRole,refreshedToken)






            }
        }
    }


    //do not remove this

    override fun onStart() {
        super.onStart()
        val mAuth = FirebaseAuth.getInstance().currentUser
        if(mAuth != null){
            appLifecycleCallback = (application as MyApp).appLifecycleCallback
            var  intent:Intent= Intent(this, HomePage::class.java)
            startActivity(intent)
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




}





