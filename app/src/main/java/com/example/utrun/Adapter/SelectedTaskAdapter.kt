package com.example.utrun.Adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.opengl.Visibility
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.Activity.task_details
import com.example.utrun.Fragment.OutGoingFragment
import com.example.utrun.R
import com.example.utrun.models.SelectedTask
import com.example.utrun.models.Tasks
import com.example.utrun.util.intents
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SelectedTaskAdapter(private var selectedTasksList: List<SelectedTask> ,activity:Activity, context: Context) : RecyclerView.Adapter<SelectedTaskAdapter.SelectedTaskViewHolder>() {

private val act:Activity = activity
private val ctx:Context = context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedTaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.selected_task_card, parent, false)
        return SelectedTaskViewHolder(view,act,selectedTasksList,ctx)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: SelectedTaskViewHolder, position: Int) {
        val selectedTask = selectedTasksList[position]


        if (selectedTask.employeePicture != null) {
            val imageBytes = Base64.decode(selectedTask.employeePicture, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            holder.employeeImageView.setImageBitmap(bitmap)
        }
          val timestamp = selectedTask.time.toString().toLong()
          val formattedDate = convertTimestampToFormattedDate(timestamp)
            holder.txt_times.text = "Time selected: ${formattedDate.toString()}"
            holder.employeeName.text = "User name: ${selectedTask.employeeName}"
            holder.employeeSurname.text = "User surname: ${selectedTask.employeeSurname}"
            holder.taskCode.text = "Task code: ${selectedTask.taskCode}"
            holder.pickLocation.text = "Pickup location: ${selectedTask.pickLocation}"
            holder.dropLocation.text = "Drop location: ${selectedTask.dropLocation}"
            holder.typeOfGoods.text = "Goods: ${selectedTask.typeOfGoods}"
            holder.carBrand.text = "Car brand: ${selectedTask.brand}";
            holder.numberPlate.text = "Number plate: ${selectedTask.numberPlate}";

            if (FirebaseAuth.getInstance().uid.toString() == selectedTask.UID) {
                holder.select_task_buttons.visibility = View.VISIBLE
            } else {
                holder.select_task_buttons.visibility = View.INVISIBLE
            }


    }
    fun convertTimestampToFormattedDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM HH:mm:ss", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }

    override fun getItemCount(): Int {
        return selectedTasksList.size
    }

    fun setFilteredList(filteredList: List<SelectedTask>) {
        this.selectedTasksList = filteredList
        notifyDataSetChanged() // Notify the adapter that the data has changed
    }

    class SelectedTaskViewHolder(itemView: View,activity: Activity,selectedTasksList: List<SelectedTask>,context: Context) : RecyclerView.ViewHolder(itemView) {
        val employeeImageView: CircleImageView = itemView.findViewById(R.id.employeeImageView)
        val employeeName: TextView = itemView.findViewById(R.id.txt_employeeName)
        val employeeSurname: TextView = itemView.findViewById(R.id.txt_employeeSurname)
        val taskCode: TextView = itemView.findViewById(R.id.txt_ID)
        val pickLocation: TextView = itemView.findViewById(R.id.txt_pickLocation)
        val dropLocation: TextView = itemView.findViewById(R.id.txt_dropLocation)
        val typeOfGoods: TextView = itemView.findViewById(R.id.txt_typeOfGoods)
        val  carBrand: TextView = itemView.findViewById(R.id.txt_carBrand)
        val  numberPlate: TextView = itemView.findViewById(R.id.txt_numberPlate)
        val  txt_times: TextView = itemView.findViewById(R.id.txt_time)
        val  select_task_buttons: TextView = itemView.findViewById(R.id.select_task_button)

        init {

            itemView.findViewById<TextView>(R.id.select_task_button).setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedTask = selectedTasksList[position]
                    val intent = Intent(activity, task_details::class.java)
                    val sharedPref = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
                    val editor = sharedPref.edit()
                    editor.putString("profile", selectedTask.employeePicture)
                    editor.apply()
                    // Add any data you need to pass to the new activity
                    intent.putExtra("uid",selectedTask.UID)
                   // intent.putExtra("profile",selectedTask.employeePicture)
                    intent.putExtra("pick",selectedTask.pickLocation)
                    intent.putExtra("drop",selectedTask.dropLocation)
                    intent.putExtra("task",selectedTask.taskCode)
                    intent.putExtra("vehicle",selectedTask.brand)
                    intent.putExtra("NumberPlate",selectedTask.numberPlate)
                    intent.putExtra("Key",selectedTask.Key)
                    intent.putExtra("bool",true)

                    // Start the new activity
                    activity.startActivity(intent)
                    activity.finish()
                }
            }
        }
    }
}
