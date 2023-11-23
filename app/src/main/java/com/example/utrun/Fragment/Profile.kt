package com.example.utrun.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.utrun.HomePage
import com.example.utrun.MainActivity
import com.example.utrun.R
import com.example.utrun.Service.AppStateService
import com.example.utrun.databinding.FragmentProfileBinding
import com.example.utrun.util.cuurentLoaction
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream
import java.io.IOException



class Profile : Fragment() {

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageBitmap: Bitmap? = null

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference()
    private var locationManager: LocationManager? = null
    private var myLatitude: Double = 0.0
    private var myLongitude: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        binding.LogOut.setOnClickListener(){
           Toast.makeText(requireContext(),"See you soon",Toast.LENGTH_LONG).show()
            FirebaseAuth.getInstance().signOut()
            val intent:Intent= Intent(requireActivity(),MainActivity::class.java)
            startActivity(intent)





        }


        binding.removeCar.setOnClickListener(){
            val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setOnClickListener

            // ... (Your existing code to get currentTimeMillis)

            // Check if the user is logged in
            if (currentUserUid.isNotEmpty()) {
                var vehicleFound = false

                FirebaseDatabase.getInstance().reference.child("vehicles")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (taskSnapshot in snapshot.children) {
                                val id = taskSnapshot.key ?: continue
                                val keys =
                                    taskSnapshot.child("key").getValue(String::class.java) ?: ""
                                val numberPlate =
                                    taskSnapshot.child("numberPlate").getValue(String::class.java)
                                        ?: ""

                                if (keys == currentUserUid) {
                                    vehicleFound = true

                                    taskSnapshot.ref.child("isAvailable").setValue(true)
                                    taskSnapshot.ref.child("key").setValue("")
                                    break // Exit the loop as vehicle is found
                                }
                            }
                            if (!vehicleFound) {
                                Toast.makeText(context, "No vehicle found", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                Toast.makeText(context, "Car has been removed", Toast.LENGTH_SHORT)
                                    .show()
                                binding.removeCar.text = "No car was selected"
                                binding.removeCar.isClickable = false;
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
            }
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Populate the fields from Firebase
        populateFields()

        binding.btnChangePic.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }

        binding.btnSaveChanges.setOnClickListener {
            saveChanges()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            val filePath = data.data
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, filePath)

                val selectedImageBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, filePath)
                val encodedImage = convertBitmapToBase64(selectedImageBitmap)
                setProfileImage(encodedImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveChanges() {

        val userId = auth.currentUser?.uid
        val updatedName = binding.editTextName.text.toString()
        val updatedSurname = binding.editTextSurname.text.toString()
        val currentPassword = binding.editTextCurrentPassword.text.toString()
        val newPassword = binding.editTextNewPassword.text.toString()

        val updates = hashMapOf(
            "name" to updatedName,
            "surname" to updatedSurname
        ).apply {
            if (newPassword.isNotBlank()) {
                put("password", newPassword)
            }
            selectedImageBitmap?.let {
                val baos = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val bytes = baos.toByteArray()
                val encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT)
                put("Picture", encodedImage)
            }
        }

        userId?.let {
            database.child("login").child("email").child(it).updateChildren(updates as Map<String, Any>)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (newPassword.isNotBlank()) {
                            updateUserPassword(currentPassword, newPassword)
                        } else {
                            Toast.makeText(activity, "Profile updated!", Toast.LENGTH_SHORT).show()
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

                                                        setLocation.setValue(nameSurnameMap)

                                                    .addOnSuccessListener {
                                                        val intent:Intent = Intent(requireActivity(),MainActivity::class.java)
                                                        startActivity(intent)
                                                    }
                                            }
                                        }

                                    }




                                    override fun onCancelled(error: DatabaseError) {
                                        // Handle onCancelled event
                                    }
                                })
                        }
                    } else {
                        Toast.makeText(activity, "Error updating profile.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun updateUserPassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser
        val credential = EmailAuthProvider.getCredential(user?.email!!, currentPassword)

        user?.reauthenticate(credential)?.addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.updatePassword(newPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(activity, "Password updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(activity, "Error updating password.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(activity, "Current password is incorrect.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun populateFields() {
        val userId = auth.currentUser?.uid

        userId?.let {
            database.child("login").child("email").child(it).get().addOnSuccessListener { dataSnapshot ->
                val name = dataSnapshot.child("name").value as? String
                val surname = dataSnapshot.child("surname").value as? String

                val profilePicBase64 = dataSnapshot.child("Picture").value as? String

                binding.editTextName.setText(name)
                binding.editTextSurname.setText(surname)

                profilePicBase64?.let {
                    setProfileImage(it)
                }
            }
        }
    }

    private fun setProfileImage(encodedImage: String) {
        val decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT)
        val decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)

        Glide.with(this@Profile)
            .load(decodedBitmap)
            .circleCrop()
            .into(binding.profileImageView)
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onResume() {
        super.onResume()
        // Show toast message when returning to the fragment
        //  Toast.makeText(context, "Return back", Toast.LENGTH_SHORT).show()
        val obj: cuurentLoaction = cuurentLoaction()
        obj.setUserCurrentLocation(requireContext())
    }
}