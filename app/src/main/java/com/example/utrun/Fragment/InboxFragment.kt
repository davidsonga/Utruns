package com.example.utrun.Fragment

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.Adapter.TaskCardAdapter
import com.example.utrun.R
import com.example.utrun.models.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date

class InboxFragment : Fragment(), TaskCardAdapter.TaskClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var taskCardAdapter: TaskCardAdapter
    private lateinit var databaseReference: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inbox, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize Firebase Database
        databaseReference = FirebaseDatabase.getInstance().reference

        // Retrieve and display data from Firebase
        retrieveDataFromFirebase()

        return view
    }


    private fun retrieveDataFromFirebase() {
        val tasksRef = databaseReference.child("tasks")

        tasksRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tasksList = ArrayList<Tasks>()

                for (taskSnapshot in snapshot.children) {
                    val Id = taskSnapshot.key
                    val dropLocationId = taskSnapshot.child("dropoffLocationId").getValue(String::class.java) ?: ""
                    val pickupLocation = taskSnapshot.child("pickupLocation").getValue(String::class.java) ?: ""
                    val uploadedTimestamp = taskSnapshot.child("uploadedTimestamp").getValue(Long::class.java) ?: 0
                    val typeOfGoods = taskSnapshot.child("typeOfGoods").getValue(String::class.java) ?: ""
                    val employeeUID = taskSnapshot.child("employeeUid").getValue(String::class.java) ?: ""
                    // Fetch location details based on dropLocationId
                    val locationsRef = databaseReference.child("locations").child(dropLocationId)
                    locationsRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(locationSnapshot: DataSnapshot) {
                            val address = locationSnapshot.child("address").getValue(String::class.java) ?: ""
                            val name = locationSnapshot.child("name").getValue(String::class.java) ?: ""

                            val type = locationSnapshot.child("type").getValue(String::class.java) ?: ""

                            // Convert timestamp to a readable format
                            //val time = convertTimestampToNormalTime(uploadedTimestamp)
                            val date = Date(uploadedTimestamp)


                            val dateFormat = SimpleDateFormat("dd MMM HH:mm:ss")


                            val time = dateFormat.format(date)
                           if(employeeUID.isNullOrEmpty()){
                               val task = Tasks(dropLocationId, address, name, pickupLocation, time, typeOfGoods,type,Id)
                               tasksList.add(task)

                               // Initialize and bind the adapter to the RecyclerView
                               taskCardAdapter = TaskCardAdapter(tasksList, this@InboxFragment)
                               recyclerView.adapter = taskCardAdapter
                           }

                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle any errors related to retrieving location data
                        }
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors related to retrieving tasks data
            }
        })
    }

    private fun convertTimestampToNormalTime(timestamp: Long): String {
        // Implement the timestamp conversion logic (e.g., using SimpleDateFormat)
        return timestamp.toString()
    }

    override fun onTaskSelected(task: Tasks) {
        // Handle item selection
    }


}
