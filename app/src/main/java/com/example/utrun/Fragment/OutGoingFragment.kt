package com.example.utrun.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
            // Start the new activity
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
        selectedTaskAdapter = SelectedTaskAdapter(selectedTasksList,requireActivity(),requireContext()) // Use the correct adapter class

        recyclerView = view.findViewById(R.id.selected_task_recycler_view)
        recyclerView.adapter = selectedTaskAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().reference

        val locationRef = databaseReference.child("locations")
        val task = databaseReference.child("tasks")
        val loginRef = databaseReference.child("login")
        val vehiclesRef = databaseReference.child("vehicles")


        task.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (taskSnapshot in snapshot.children) {
                    val key =taskSnapshot.key // Get the unique key t
                    val employeeUid = taskSnapshot.child("employeeUid").getValue(String::class.java)
                    val pickupLocation = taskSnapshot.child("pickupLocation").getValue(String::class.java)
                    val typeOfGoods = taskSnapshot.child("typeOfGoods").getValue(String::class.java)
                    val assignedTimestampLong = taskSnapshot.child("assignedTimestamp").getValue(Long::class.java)
                    val vehiclKey = taskSnapshot.child("vehicleId").getValue(String::class.java)
                    val dropID = taskSnapshot.child("dropoffLocationId").getValue(String::class.java)
                    val completedTimestamp = taskSnapshot.child("completedTimestamp").getValue(Long::class.java)


                    if (key != null) {
                        // Fetch data from the 'vehicles' node
                        vehiclesRef.child(vehiclKey.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(vehiclesSnapshot: DataSnapshot) {
                                val brand = vehiclesSnapshot.child("brand").getValue(String::class.java)
                                val numberPlate = vehiclesSnapshot.child("numberPlate").getValue(String::class.java)


                                loginRef.child("email").child(employeeUid.toString()).addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(loginSnapshot: DataSnapshot) {
                                        val employeePicture = loginSnapshot.child("Picture").getValue(String::class.java)
                                        val employeeName = loginSnapshot.child("name").getValue(String::class.java)
                                        val employeeSurname = loginSnapshot.child("surname").getValue(String::class.java)

                                        locationRef.child(dropID.toString()).addValueEventListener(object : ValueEventListener {
                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                val dropoff = snapshot.child("address").getValue(String::class.java)
                                                val name = snapshot.child("name").getValue(String::class.java)

                                                // Format the timestamp as a String if needed
                                                val assignedTimestamp = assignedTimestampLong?.toString() ?: ""
                                                if(completedTimestamp ==0L && !employeeUid.isNullOrEmpty()){
                                                    val selectedTask = SelectedTask(
                                                        employeePicture,
                                                        employeeName,
                                                        employeeSurname,
                                                        dropID,
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

                                                    // Add the SelectedTask to the list and notify the adapter
                                                    selectedTasksList.add(selectedTask)
                                                    selectedTaskAdapter.notifyDataSetChanged()
                                                }

                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                // Handle errors
                                            }
                                        })
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        // Handle errors
                                    }
                                })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle errors
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
        return view
    }
}
