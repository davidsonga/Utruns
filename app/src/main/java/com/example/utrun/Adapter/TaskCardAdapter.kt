package com.example.utrun.Adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.Activity.SelectCar
import com.example.utrun.Fragment.InboxFragment
import com.example.utrun.R
import com.example.utrun.models.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskCardAdapter(private var tasksList: List<Tasks>, private val listener: InboxFragment,context:Context,act: Activity) : RecyclerView.Adapter<TaskCardAdapter.TaskViewHolder>() {

    private var selectedTask: Tasks? = null
    private var cnt:Context = context
    private var activity:Activity =act
    interface TaskClickListener {
        fun onTaskSelected(task: Tasks)
    }

    fun getSelectedTask(): Tasks? {
        return selectedTask
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.task_card_item, parent, false)
        return TaskViewHolder(view, listener)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilteredList(filteredList: List<Tasks>) {
        tasksList = filteredList
        notifyDataSetChanged()
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasksList[position]
        holder.id.text = "Task Code: ${task.userID}"
        holder.txt_placeNames.text = "Organization: ${task.placeNames}"
        holder.pickLocation.text = "Pick up location: ${task.pickLocation}"
        holder.dropLocation.text = "Drop location: ${task.dropLocation}"
        holder.timeUploaded.text = "Time uploaded: ${task.time}"
        holder.typeOfGoods.text = "Goods: ${task.typeOfGoods}"
        holder.txt_types.text = "Type: ${task.txt_types}"

        // Highlight the selected task
        if (task == selectedTask) {
            holder.itemView.setBackgroundColor(Color.LTGRAY)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int {
        return tasksList.size
    }

    inner class TaskViewHolder(itemView: View, listener: TaskClickListener) : RecyclerView.ViewHolder(itemView) {
        val txt_placeNames: TextView = itemView.findViewById(R.id.txt_placeName)
        val id: TextView = itemView.findViewById(R.id.txt_selectedTaskCode)
        val timeUploaded: TextView = itemView.findViewById(R.id.txt_upload)
        val pickLocation: TextView = itemView.findViewById(R.id.txt_pickLocation)
        val dropLocation: TextView = itemView.findViewById(R.id.txt_dropLocation)
        val typeOfGoods: TextView = itemView.findViewById(R.id.txt_selectedTypeOfGoods)
        val txt_types: TextView = itemView.findViewById(R.id.txt_type)

        init {
            itemView.findViewById<TextView>(R.id.select_task_button).setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedTask = tasksList[position]

                    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

                    val currentTime = Date()
                    val dateFormat = SimpleDateFormat("dd MMM HH:mm:ss", Locale.getDefault())
                    dateFormat.format(currentTime)
                    val currentTimeMillis = System.currentTimeMillis()
                    // Check if the user is logged in
                    if (currentUserUid != null) {
                        var vehicleFound = false
                        // Update the employeeUid in the selected task
                        FirebaseDatabase.getInstance().reference.child("vehicles")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    for (taskSnapshot in snapshot.children) {
                                        val id = taskSnapshot.key ?: continue
                                        val keys = taskSnapshot.child("key").getValue(String::class.java) ?: ""
                                        val numberPlate = taskSnapshot.child("numberPlate").getValue(String::class.java) ?: ""

                                        if (keys == currentUserUid) {
                                            vehicleFound = true
                                            val taskReference =
                                                FirebaseDatabase.getInstance().reference.child("tasks")
                                                    .child(selectedTask.uniqueID.toString())
                                            taskReference.child("employeeUid")
                                                .setValue(currentUserUid)
                                            taskReference.child("assignedTimestamp")
                                                .setValue(currentTimeMillis)
                                            taskReference.child("vehicleId").setValue(id)
                                            break // Exit the loop as vehicle is found
                                        }
                                    }
                                    if (!vehicleFound) {
                                        Toast.makeText(cnt, "You need to select a vehicle before selecting a task", Toast.LENGTH_SHORT).show()
                                        val intent:Intent = Intent(activity,SelectCar::class.java)
                                        activity.startActivity(intent)
                                    }else{




                                        // listener.onTaskSelected(selectedTask)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                 //   Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                        val mutableTasksList: MutableList<Tasks> = tasksList.toMutableList()
                        mutableTasksList.removeAt(position)


                        tasksList = mutableTasksList


                        notifyItemRemoved(position)

                    }
                }


            }
        }
    }
}
