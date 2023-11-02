package com.example.utrun.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.utrun.MainActivity
import com.example.utrun.R
import com.example.utrun.util.intents
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.ByteArrayOutputStream

class task_details : AppCompatActivity() {
    private lateinit var imageView1:ImageView
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private  var uniqueID:String=""
    private  var goodsPick:String=""
    private var name:String?=""
    private  var dropID:String =""
    private  var Keys:String =""

    private  var isBool:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)
     val txt_TaskCode:TextView = findViewById(R.id.txt_TaskCode)
     val txt_vehicle:TextView = findViewById(R.id.txt_vehicle)
     val txt_Task:TextView = findViewById(R.id.txt_Task)
     val txt_Drop:TextView = findViewById(R.id.txt_Drop)
     val user_image:ImageView = findViewById(R.id.user_image)
     val btn_finishOnboarding:Button = findViewById(R.id.btn_finishOnboarding)
     val btn_TakePhoto: Button = findViewById(R.id.btn_TakePhoto)



        val UID = intent.getStringExtra("uid")
        Keys = intent.getStringExtra("Key").toString()
        val sharedPref = this.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val profile = sharedPref.getString("profile", "")
        val pickLocation = intent.getStringExtra("pick")
        val task = intent.getStringExtra("task")
        val dropLocation = intent.getStringExtra("drop")
        val vehicle = intent.getStringExtra("vehicle")
        val NumberPlate = intent.getStringExtra("NumberPlate")
        isBool = intent.getBooleanExtra("bool",false)

        //converting string to bitmap

        val imageBytes = Base64.decode(profile, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        user_image.setImageBitmap(bitmap)

        txt_vehicle.text = "Vehicle: ${vehicle}"
        txt_Task.text =  "PickUp: ${pickLocation}"
        txt_Drop.text = "Drop: ${dropLocation}"
        val databaseReference = FirebaseDatabase.getInstance().reference

        //get tasks values
        val tasksRef = FirebaseDatabase.getInstance().reference.child("tasks")

        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (taskSnapshot in snapshot.children) {
                    val uniqueKey = taskSnapshot.key // Get the unique key for each task
                    val dropoffLocationId = taskSnapshot.child("dropoffLocationId").getValue(String::class.java)
                    val goods = taskSnapshot.child("typeOfGoods").getValue(String::class.java)
                    val employeeUid = taskSnapshot.child("employeeUid").getValue(String::class.java)


                    if (dropoffLocationId == task) {

                        uniqueID = uniqueKey.toString()
                        goodsPick = goods.toString()
                        dropID =dropoffLocationId.toString()



                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
        val locData =  FirebaseDatabase.getInstance().reference.child("locations")
        locData.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(snap in snapshot.children){
                    val keyys= snap.key
                    val names= snap.child("name").getValue(String::class.java)

                    if(dropID == keyys){
                        name=names.toString()
                        txt_TaskCode.text ="Company name: ${name}"
                    }



                }


            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        btn_finishOnboarding.setOnClickListener(){
            imageView1 = findViewById(R.id.imageView1)
            // Get the drawable from the ImageView
            val drawable = imageView1.drawable

// Convert the drawable to a Bitmap
            val bitmap: Bitmap = (drawable as BitmapDrawable).bitmap
            uploadFinshishedTask(UID,pickLocation,task,dropLocation,NumberPlate,vehicle, bitmapToBase64(bitmap))
        }
        btn_TakePhoto.setOnClickListener {
            // Create an intent to capture an image using the device's camera
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // Check if there's a camera activity available to handle the intent
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imageView1  = findViewById(R.id.imageView1)
        // Check if the request was successful
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                // If the request was for taking a photo with the camera
                val imageBitmap = data.extras?.get("data") as Bitmap?
                imageView1?.setImageBitmap(imageBitmap)
            } else if (requestCode == REQUEST_IMAGE_PICK && data != null) {
                // If the request was for picking an image from the gallery
                val selectedImage = data.data
                val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                imageView1?.setImageBitmap(imageBitmap)
            }
        }
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }


    @SuppressLint("SuspiciousIndentation")
    private fun uploadFinshishedTask(uid: String?, pickLocation: String?, task: String?, dropLocation: String?, NumberPlate:String?, vehicle: String?, bitmapToBase64: String) {

        //upload uid

        val dutyCompleted = FirebaseDatabase.getInstance().reference.child("dutyCompleted")
            .child(Keys)
        dutyCompleted.child("PickLocation").setValue(pickLocation)
        dutyCompleted.child("DropLocation").setValue(dropLocation)
        dutyCompleted.child("vehicle").setValue(vehicle)
        dutyCompleted.child("NumberPlate").setValue(NumberPlate)
        dutyCompleted.child("Picture").setValue(bitmapToBase64)
        dutyCompleted.child("CompanyName").setValue(name)
        dutyCompleted.child("TypeOfGoods").setValue(goodsPick)
        val currentTimeMillis = System.currentTimeMillis()
        val tasks = FirebaseDatabase.getInstance().reference.child("tasks")
           tasks.addValueEventListener(object :ValueEventListener{
               override fun onDataChange(snapshot: DataSnapshot) {
                   if(isBool){
                       for(taskSnapshot in snapshot.children){

                           val completedTimestamp =taskSnapshot.child("completedTimestamp").getValue(Long::class.java)
                           val employeeUid = taskSnapshot.child("employeeUid").getValue(String::class.java)
                           val currentJob = employeeUid+Keys
                           val currentUser = FirebaseAuth.getInstance().uid+Keys
                           if(currentJob ==currentUser &&completedTimestamp ==0L ){
                               tasks.child(Keys).child("completedTimestamp").setValue(currentTimeMillis)
                               tasks.child(Keys).child("ratingId").setValue(Keys)

                           }

                   }
                       isBool =false



                   }

               }

               override fun onCancelled(error: DatabaseError) {
                   TODO("Not yet implemented")
               }

           })
        Toast.makeText(this,"Finish task has been submitted",Toast.LENGTH_LONG).show()
        val intent :Intent = Intent(this, Rate::class.java)
        intent.putExtra("taskID",task)
        startActivity(intent)
        finish()

    }
}