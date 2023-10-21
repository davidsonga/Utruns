package com.example.utrun.Network


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import com.example.utrun.HomePage
import com.example.utrun.Service.AppLifecycleCallback
import com.example.utrun.Service.MyApp

import com.example.utrun.util.intents
import com.example.utrun.util.progressDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.IOException

class Upload {

    private var isTrue: Boolean = false
   // private var objProgress: progressDialog = progressDialog()
    private var objIntent:intents =intents()
    var objProgress:progressDialog= progressDialog()
    private lateinit var appLifecycleCallback: AppLifecycleCallback
    fun uploadProfilePicture(activity: Activity, selectedImageUri: Uri, ) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var activity2: HomePage = HomePage()
        if (currentUser != null) {
            val uid = currentUser.uid

            try {
                val inputStream = activity.contentResolver.openInputStream(selectedImageUri)
                if (inputStream != null) {
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)
                    var len: Int

                    while (inputStream.read(buffer).also { len = it } != -1) {
                        byteArrayOutputStream.write(buffer, 0, len)
                    }

                    val imageByteArray = byteArrayOutputStream.toByteArray()
                    val base64Image = android.util.Base64.encodeToString(imageByteArray, android.util.Base64.DEFAULT)

                    // Update the user's picture in the Realtime Database as a Base64 encoded string
                    val userReference = FirebaseDatabase.getInstance().reference
                        .child("login").child("email").child(uid)
                    userReference.child("Picture").setValue(base64Image)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                appLifecycleCallback = (activity.application as MyApp).appLifecycleCallback
                                // Display success message if the update was successful
                                Toast.makeText(activity, "Profile picture uploaded successfully", Toast.LENGTH_LONG).show()
                               objProgress.isProgressDialogDisable()
                                objIntent.intent(activity, activity2 )
                                // Finish the onboarding process or navigate to the next activity
                                // Add your navigation logic here
                            } else {
                                // Display error message if the update failed
                                objProgress.isProgressDialogDisable()
                                Toast.makeText(activity, "Error uploading profile picture", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            } catch (e: IOException) {
                e.printStackTrace()

                // Display error message if upload fails
                Toast.makeText(activity, "Error uploading profile picture", Toast.LENGTH_LONG).show()
            }
        }
    }


}