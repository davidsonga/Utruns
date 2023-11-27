package com.example.utrun.Network



import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.utrun.Activity.SelectCar
import com.example.utrun.HomePage
import com.example.utrun.Service.AppLifecycleCallback
import com.example.utrun.Service.MyApp

import com.example.utrun.Activity.UploadUserImageView
import com.example.utrun.util.cuurentLoaction
import com.example.utrun.util.intents
import com.example.utrun.util.progressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginProgress {
    private var isTrue:Boolean=false
     var isProfilePicture:Boolean=false
    private var objProgress:progressDialog= progressDialog()
    private lateinit var appLifecycleCallback: AppLifecycleCallback

   fun isLoginUser(acitivity: Activity, email: String, password: String, role: String, refreshedToken:String):Boolean{

        val auth = FirebaseAuth.getInstance()
       objProgress.isProgressDialogEnable(acitivity,"Please wait...")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(acitivity) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Check the user's role after successful login
                    isCheckUserRole(acitivity,user?.uid, role,refreshedToken)

                } else {
                    objProgress.isProgressDialogDisable()
                    Toast.makeText(acitivity,"user details does not exist", Toast.LENGTH_LONG).show()

                }
            }

       return isTrue
    }



    fun isCheckUserRole(activity: Activity, userId: String?, expectedRole: String, userToken: String) {
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().reference
            val usersReference = databaseReference.child("login").child("email").child(userId)

            usersReference.addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("SuspiciousIndentation")
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    val picture = dataSnapshot.child("Picture").getValue(String::class.java)?:""
                    var objintent:intents=intents()
                    val activity2: UploadUserImageView = UploadUserImageView()
                    val activity3: HomePage = HomePage()


                   // if (userRole == expectedRole) {
                       // isTrue = true
                        objProgress.isProgressDialogDisable()
                        // I used picture.length < 5 because if picture is empty and you use isNull it returns a wrong answer
                        if (picture.isEmpty() || picture == "") {
                          //  appLifecycleCallback = (activity.application as MyApp).appLifecycleCallback
                            // Picture field is empty
                            objProgress.isProgressDialogDisable()
                            isProfilePicture = false
                            Toast.makeText(activity, "Login success, please provide a profile picture", Toast.LENGTH_LONG).show()
                            val current:cuurentLoaction = cuurentLoaction()
                            current.setUserCurrentLocation(activity)
                            objintent.intent(activity, activity2)
                        } else {
                            // Picture field is not empty
                            objProgress.isProgressDialogDisable()
                            isProfilePicture = true
                            Toast.makeText(activity, "Login success", Toast.LENGTH_LONG).show()
                            val  intent: Intent = Intent(activity, activity3::class.java)
                            activity.startActivity(intent)
                        }

                        // Add the FCM token to the user's data
                        usersReference.child("FCMToken").setValue(userToken)
                   /* } else {
                        isTrue = false
                        objProgress.isProgressDialogDisable()
                        Toast.makeText(activity, "Role does not match, user is not authorized", Toast.LENGTH_LONG).show()
                    }*/
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    isTrue = false
                    objProgress.isProgressDialogDisable()
                    Toast.makeText(activity, "Error has happened", Toast.LENGTH_LONG).show()
                }
            })
        }


    }



}