package com.example.utrun.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.example.utrun.R
import com.example.utrun.models.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TaskAddedByTheUser : AppCompatActivity() {

    private lateinit var backButton: Button
    private lateinit var addTaskButton: Button
    private lateinit var pickupLocationEditText: EditText
    private lateinit var dropoffLocationEditText: EditText
    private lateinit var typeOfGoodsEditText: EditText
    private lateinit var typeEditText: EditText

    // Firebase reference
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val tasksReference = firebaseDatabase.getReference("tasks")
    private lateinit var dropOffLocationSpinner: Spinner
    private var locationsMap = HashMap<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_added_by_the_user)
        // Initialize views
        backButton = findViewById(R.id.btn_back)
        addTaskButton = findViewById(R.id.btn_newBird)
        pickupLocationEditText = findViewById(R.id.scientific_PickUpLocation)
        typeOfGoodsEditText = findViewById(R.id.typeOfGoods)
        typeEditText = findViewById(R.id.typeEt)
        dropOffLocationSpinner = findViewById(R.id.spinner_dropOffLocation)
        loadLocationsIntoSpinner()

        addTaskButton.setOnClickListener {
            if (validateInputs()) {
                addTaskToDatabase()
            }
        }
    }

    private fun loadLocationsIntoSpinner() {
        val locationNames = ArrayList<String>()
        firebaseDatabase.getReference("locations").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (locationSnapshot in snapshot.children) {
                    val name = locationSnapshot.child("name").getValue(String::class.java) ?: continue
                    val id = locationSnapshot.key ?: continue
                    locationsMap[name] = id
                    locationNames.add(name)
                }
                val adapter = ArrayAdapter(this@TaskAddedByTheUser, android.R.layout.simple_spinner_item, locationNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                dropOffLocationSpinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TaskAddedByTheUser, "Error loading locations: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun isValidAddress(address: String): Boolean {
        // Regular Expression for basic address validation
        val addressRegex = "^[a-zA-Z0-9,\\s]+".toRegex()
        return address.isNotEmpty() && addressRegex.matches(address)
    }

    private fun validateInputs(): Boolean {
        val pickupLocation = pickupLocationEditText.text.toString()
        val typeOfGoods = typeOfGoodsEditText.text.toString()
        val type = typeEditText.text.toString()

        if (pickupLocation.isEmpty()  || typeOfGoods.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isValidAddress(pickupLocation)) {
            Toast.makeText(this, "Please enter valid addresses", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun addTaskToDatabase() {
        val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
        if (currentUserUid != null) {
            FirebaseDatabase.getInstance().reference.child("vehicles")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var vehicleFound = false
                        var vehicleId = ""
                        var vehicleBrand = ""
                        var vehicleNumberPlate = ""

                        for (vehicleSnapshot in snapshot.children) {
                            val id = vehicleSnapshot.key ?: continue
                            val keys = vehicleSnapshot.child("key").getValue(String::class.java) ?: ""
                            if (keys == currentUserUid) {
                                vehicleFound = true
                                vehicleId = id
                                vehicleBrand = vehicleSnapshot.child("brand").getValue(String::class.java) ?: ""
                                vehicleNumberPlate = vehicleSnapshot.child("numberPlate").getValue(String::class.java) ?: ""
                                break
                            }
                        }

                        if (!vehicleFound) {
                            // Redirect to vehicle selection
                            val intent = Intent(this@TaskAddedByTheUser, SelectCar::class.java)
                            startActivity(intent)
                        } else {
                            // Proceed with adding the task
                            createAndAddTask(vehicleId, vehicleBrand, vehicleNumberPlate)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@TaskAddedByTheUser, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun createAndAddTask(vehicleId: String, vehicleBrand: String, vehicleNumberPlate: String) {
        val pickupLocation = pickupLocationEditText.text.toString()
        val dropoffLocationName = dropOffLocationSpinner.selectedItem.toString()
        val typeOfGoods = typeOfGoodsEditText.text.toString()
        val type = typeEditText.text.toString()

        // Get the current time in milliseconds
        val currentTimeMillis = System.currentTimeMillis()

        // Assuming you have a method to get the current logged-in user's UID
        val employeeUid = getCurrentUserUid()

        // Retrieve the drop-off location ID from the spinner selection
        val dropoffLocationId = locationsMap[dropoffLocationName]

        if (dropoffLocationId != null && dropoffLocationId.isNotEmpty()) {
            // Create a new Task object with all the details
            val newTask = Task(
                pickupLocation = pickupLocation,
                dropoffLocationId = dropoffLocationId, // Use dropoffLocationId here
                typeOfGoods = typeOfGoods,
                type = type,
                employeeUid = employeeUid,
                vehicleId = vehicleId,
                vehicleBrand = vehicleBrand,
                vehicleNumberPlate = vehicleNumberPlate,
                assignedTimestamp = currentTimeMillis
            )

            // Add the new task to the Firebase database
            tasksReference.push().setValue(newTask)
                .addOnSuccessListener {
                    Toast.makeText(this@TaskAddedByTheUser, "Task added successfully", Toast.LENGTH_SHORT).show()
                    clearInputFields()
                }
                .addOnFailureListener {
                    Toast.makeText(this@TaskAddedByTheUser, "Failed to add task", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this@TaskAddedByTheUser, "Invalid drop-off location name", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearInputFields() {
        pickupLocationEditText.text.clear()
        typeOfGoodsEditText.text.clear()
        typeEditText.text.clear()
    }

    private fun getCurrentUserUid(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
}