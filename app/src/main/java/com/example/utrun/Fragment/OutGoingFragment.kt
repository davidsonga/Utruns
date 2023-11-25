package com.example.utrun.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.Activity.TaskAddedByTheUser
import com.example.utrun.Adapter.SelectedTaskAdapter
import com.example.utrun.Adapter.TaskCardAdapter
import com.example.utrun.R
import com.example.utrun.models.SelectedTask
import com.example.utrun.models.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class OutGoingFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedTaskAdapter: SelectedTaskAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var addNewTaskButton: ImageButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addNewTaskButton = view.findViewById(R.id.btn_addTask)
        addNewTaskButton.setOnClickListener {
            val intent = Intent(activity, TaskAddedByTheUser::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_out_going, container, false)
        val selectedTasksList: MutableList<SelectedTask> = mutableListOf()
        selectedTaskAdapter = SelectedTaskAdapter(selectedTasksList, requireActivity(), requireContext())

        recyclerView = view.findViewById(R.id.selected_task_recycler_view)
        recyclerView.adapter = selectedTaskAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        databaseReference = FirebaseDatabase.getInstance().reference
        fetchTasks(selectedTasksList)

        return view
    }

    private fun fetchTasks(selectedTasksList: MutableList<SelectedTask>) {
        val taskRef = databaseReference.child("tasks")
        val vehiclesRef = databaseReference.child("vehicles")
        val loginRef = databaseReference.child("login")
        val locationRef = databaseReference.child("locations")

        taskRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                selectedTasksList.clear()
                for (taskSnapshot in snapshot.children) {
                    val key = taskSnapshot.key
                    val employeeUid = taskSnapshot.child("employeeUid").getValue(String::class.java)
                    val pickupLocation = taskSnapshot.child("pickupLocation").getValue(String::class.java)
                    val typeOfGoods = taskSnapshot.child("typeOfGoods").getValue(String::class.java)
                    val assignedTimestamp = taskSnapshot.child("assignedTimestamp").getValue(Long::class.java)?.toString()
                    val vehicleId = taskSnapshot.child("vehicleId").getValue(String::class.java)
                    val dropoffLocationId = taskSnapshot.child("dropoffLocationId").getValue(String::class.java)
                    val completedTimestamp = taskSnapshot.child("completedTimestamp").getValue(Long::class.java)

                    if (employeeUid != null && completedTimestamp != null && completedTimestamp == 0L) {
                        fetchVehicleDetails(vehiclesRef, vehicleId) { brand, numberPlate ->
                            fetchEmployeeDetails(loginRef, employeeUid) { employeePicture, employeeName, employeeSurname ->
                                fetchLocationDetails(locationRef, dropoffLocationId) { dropoff, name ->
                                    val selectedTask = SelectedTask(
                                        employeePicture,
                                        employeeName,
                                        employeeSurname,
                                        key,
                                        pickupLocation,
                                        dropoff,
                                        typeOfGoods,
                                        brand,
                                        numberPlate,
                                        assignedTimestamp,
                                        employeeUid,
                                        key,
                                        name
                                    )
                                    selectedTasksList.add(selectedTask)
                                    selectedTaskAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
    }

    private fun fetchVehicleDetails(vehiclesRef: DatabaseReference, vehicleKey: String?, callback: (String?, String?) -> Unit) {
        if (vehicleKey == null) {
            callback(null, null)
            return
        }
        vehiclesRef.child(vehicleKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val brand = snapshot.child("brand").getValue(String::class.java)
                val numberPlate = snapshot.child("numberPlate").getValue(String::class.java)
                callback(brand, numberPlate)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, null)
            }
        })
    }

    private fun fetchEmployeeDetails(loginRef: DatabaseReference, employeeUid: String, callback: (String?, String?, String?) -> Unit) {
        loginRef.child("email").child(employeeUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val employeePicture = snapshot.child("Picture").getValue(String::class.java)
                val employeeName = snapshot.child("name").getValue(String::class.java)
                val employeeSurname = snapshot.child("surname").getValue(String::class.java)
                callback(employeePicture, employeeName, employeeSurname)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(null, null, null)
            }
        })
    }

    private fun fetchLocationDetails(locationRef: DatabaseReference, locationKey: String?, callback: (String?, String?) -> Unit) {
        if (locationKey == null) {
            Log.e("OutGoingFragment", "Location key is null")
            callback(null, null)
            return
        }
        locationRef.child(locationKey).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val address = snapshot.child("address").getValue(String::class.java)
                val name = snapshot.child("name").getValue(String::class.java)

                if (address == null || name == null) {
                    Log.e("OutGoingFragment", "Address or Name is null for locationKey: $locationKey")
                }

                callback(address, name)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OutGoingFragment", "Error fetching location details: ${error.message}")
                callback(null, null)
            }
        })
    }
}