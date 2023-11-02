package com.example.utrun.Fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.utrun.MainActivity
import com.example.utrun.R
import com.example.utrun.Service.AppStateService
import com.example.utrun.databinding.FragmentProfileBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.IOException



class Profile : Fragment() {

    private val PICK_IMAGE_REQUEST = 1
    private var selectedImageBitmap: Bitmap? = null

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().getReference()

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
}