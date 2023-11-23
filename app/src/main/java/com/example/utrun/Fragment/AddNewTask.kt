package com.example.utrun.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.utrun.R
import com.example.utrun.models.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddNewTask : Fragment() {

    private lateinit var backButton: Button
    private lateinit var addTaskButton: Button
    private lateinit var pickupLocationEditText: EditText
    private lateinit var dropoffLocationEditText: EditText
    private lateinit var typeOfGoodsEditText: EditText
    private lateinit var typeEditText: EditText

    // Firebase reference
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val tasksReference = firebaseDatabase.getReference("tasks")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_new_task, container, false)

        // Initialize views
        backButton = view.findViewById(R.id.btn_back)
        addTaskButton = view.findViewById(R.id.btn_newBird)
        pickupLocationEditText = view.findViewById(R.id.scientific_PickUpLocation)
        dropoffLocationEditText = view.findViewById(R.id.DropOffLocationEt)
        typeOfGoodsEditText = view.findViewById(R.id.typeOfGoods)
        typeEditText = view.findViewById(R.id.typeEt)

        backButton.setOnClickListener {
            // Navigate back
            fragmentManager?.popBackStack()
        }

        addTaskButton.setOnClickListener {
            if (validateInputs()) {
                addTaskToDatabase()
            }
        }

        return view
    }

    private fun isValidAddress(address: String): Boolean {
        // Regular Expression for basic address validation
        val addressRegex = "^[a-zA-Z0-9,\\s]+".toRegex()
        return address.isNotEmpty() && addressRegex.matches(address)
    }

    private fun validateInputs(): Boolean {
        val pickupLocation = pickupLocationEditText.text.toString()
        val dropoffLocation = dropoffLocationEditText.text.toString()
        val typeOfGoods = typeOfGoodsEditText.text.toString()
        val type = typeEditText.text.toString()

        if (pickupLocation.isEmpty() || dropoffLocation.isEmpty() || typeOfGoods.isEmpty() || type.isEmpty()) {
            Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!isValidAddress(pickupLocation) || !isValidAddress(dropoffLocation)) {
            Toast.makeText(context, "Please enter valid addresses", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun addTaskToDatabase() {
        val pickupLocation = pickupLocationEditText.text.toString()
        val dropoffLocation = dropoffLocationEditText.text.toString()
        val typeOfGoods = typeOfGoodsEditText.text.toString()
        val type = typeEditText.text.toString()

        // Assuming you have a method to get the current logged-in user's UID
        val employeeUid = getCurrentUserUid()

        val newTask = Task(pickupLocation, dropoffLocation, typeOfGoods, type, employeeUid)

        tasksReference.push().setValue(newTask)
            .addOnSuccessListener {
                Toast.makeText(context, "Task added successfully", Toast.LENGTH_SHORT).show()

                fragmentManager?.beginTransaction()?.apply {
                    replace(R.id.fragment_container, UserTask())
                    addToBackStack(null)
                    commit()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to add task", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentUserUid(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        backButton = view.findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            // Navigate to UserTask fragment
            fragmentManager?.beginTransaction()?.apply {
                replace(R.id.fragment_container, UserTask())
                addToBackStack(null)
                commit()
            }
        }
    }
}