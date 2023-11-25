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
import android.text.Html
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
    private val REQUEST_IMAGE_CAPTURE = 1
    private var uniqueID: String = ""
    private var goodsPick: String = ""
    private var name: String? = ""
    private var dropID: String = ""
    private var keys: String = ""
    private var isBool: Boolean = false
    private lateinit var imageView1: ImageView
    private var imageUploaded: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_details)

        loadDataFromIntent()
        setupFirebaseListeners()
        setupUI()
    }

    private fun loadDataFromIntent() {
        val UID = intent.getStringExtra("uid")
        keys = intent.getStringExtra("Key") ?: ""
        val sharedPref = this.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        val profile = sharedPref.getString("profile", "")
        val pickLocation = intent.getStringExtra("pick")
        val task = intent.getStringExtra("task")
        val dropLocation = intent.getStringExtra("drop")
        val vehicle = intent.getStringExtra("vehicle")
        val numberPlate = intent.getStringExtra("NumberPlate")
        isBool = intent.getBooleanExtra("bool", false)

        val imageBytes = Base64.decode(profile, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        findViewById<ImageView>(R.id.user_image).setImageBitmap(bitmap)

        findViewById<TextView>(R.id.txt_TaskCode).text = Html.fromHtml("<b>Task Code:</b> $task")
        findViewById<TextView>(R.id.txt_vehicle).text = Html.fromHtml("<b>Vehicle:</b> $vehicle / <b>Number Plate:</b> $numberPlate")
        findViewById<TextView>(R.id.txt_Task).text = Html.fromHtml("<b>PickUp:</b> $pickLocation")
        findViewById<TextView>(R.id.txt_Drop).text = Html.fromHtml("<b>Drop:</b> $dropLocation")

    }

    private fun setupFirebaseListeners() {
        val tasksRef = FirebaseDatabase.getInstance().reference.child("tasks")

        tasksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (taskSnapshot in snapshot.children) {
                    val uniqueKey = taskSnapshot.key
                    val dropoffLocationId = taskSnapshot.child("dropoffLocationId").getValue(String::class.java)
                    val goods = taskSnapshot.child("typeOfGoods").getValue(String::class.java)

                    if (dropoffLocationId == dropID) {
                        uniqueID = uniqueKey.toString()
                        goodsPick = goods.toString()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })

        val locData = FirebaseDatabase.getInstance().reference.child("locations")
        locData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val keyys = snap.key
                    val names = snap.child("name").getValue(String::class.java)

                    if (dropID == keyys) {
                        name = names.toString()
                        findViewById<TextView>(R.id.txt_TaskCode).text = "Company name: $name"
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupUI() {
        imageView1 = findViewById(R.id.imageView1)

        findViewById<Button>(R.id.btn_finishOnboarding).setOnClickListener {
            if (!imageUploaded) {
                Toast.makeText(this, "Please upload an image to proceed", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val bitmap: Bitmap = (imageView1.drawable as BitmapDrawable).bitmap
            uploadFinishedTask(bitmap)
        }

        findViewById<Button>(R.id.btn_TakePhoto).setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null) {
                val imageBitmap = data.extras?.get("data") as Bitmap?
                imageView1.setImageBitmap(imageBitmap)
                imageUploaded = true // Set the flag to true as an image has been captured
            }
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    @SuppressLint("SuspiciousIndentation")
    private fun uploadFinishedTask(bitmap: Bitmap) {
        val bitmapToBase64 = bitmapToBase64(bitmap)
        val UID = FirebaseAuth.getInstance().uid ?: return

        val dutyCompleted = FirebaseDatabase.getInstance().reference.child("dutyCompleted").child(keys)
        dutyCompleted.child("PickLocation").setValue(findViewById<TextView>(R.id.txt_Task).text.toString())
        dutyCompleted.child("DropLocation").setValue(findViewById<TextView>(R.id.txt_Drop).text.toString())
        dutyCompleted.child("vehicle").setValue(findViewById<TextView>(R.id.txt_vehicle).text.toString())
        dutyCompleted.child("NumberPlate").setValue("Your Number Plate Data Here") // Replace with actual data
        dutyCompleted.child("Picture").setValue(bitmapToBase64)
        dutyCompleted.child("CompanyName").setValue(name)
        dutyCompleted.child("TypeOfGoods").setValue(goodsPick)

        val currentTimeMillis = System.currentTimeMillis()
        val tasks = FirebaseDatabase.getInstance().reference.child("tasks")
        tasks.child(keys).child("completedTimestamp").setValue(currentTimeMillis)
        tasks.child(keys).child("ratingId").setValue(keys)

        Toast.makeText(this, "Finish task has been submitted", Toast.LENGTH_LONG).show()
        val intent = Intent(this, Rate::class.java) // Replace Rate::class.java with your actual class
        intent.putExtra("taskID", "Your Task ID Here") // Replace with actual task ID
        startActivity(intent)
        finish()
    }
}