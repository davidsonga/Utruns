package com.example.utrun.Activity
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.utrun.Network.Upload
import com.example.utrun.R

class UploadUserImageView : AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imgUserProfileImage: ImageView
    private lateinit var selectedImageUri: Uri
    private var objUpload:Upload= Upload()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_user_image_view)

        imgUserProfileImage = findViewById(R.id.img_userProfileImage)
        val btnTakePhoto: Button = findViewById(R.id.btn_TakePhoto)
        val btnFinishOnboarding: Button = findViewById(R.id.btn_finishOnboarding)

        btnTakePhoto.setOnClickListener {
            openImagePicker()
        }

        btnFinishOnboarding.setOnClickListener {

            //objProgress.isProgressDialogEnable(this,"Uploading picture")

            imgUserProfileImage.setImageURI(selectedImageUri)
            objUpload.objProgress.isProgressDialogEnable(this,"Uploading picture...")
            objUpload.uploadProfilePicture(this,selectedImageUri)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data!!
            imgUserProfileImage.setImageURI(selectedImageUri)
        }
    }





}
