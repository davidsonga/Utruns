package com.example.utrun.Adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.text.Html
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.Activity.ChatAct

import com.example.utrun.Activity.task_details
import com.example.utrun.MainActivity
import com.example.utrun.R
import com.example.utrun.models.SelectedTask
import com.example.utrun.util.progressDialog
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException
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
        holder.txt_times.text = Html.fromHtml("<b>Time selected:</b> ${formattedDate.toString()}")
        holder.employeeName.text = Html.fromHtml("<b>User name:</b> ${selectedTask.employeeName}")
        holder.employeeSurname.text = Html.fromHtml("<b>User surname:</b> ${selectedTask.employeeSurname}")
        holder.taskCode.text = Html.fromHtml("<b>Organization:</b> ${selectedTask.name}<br><b>Task code:</b> ${selectedTask.taskCode}")
        holder.pickLocation.text = Html.fromHtml("<b>Pickup location:</b> ${selectedTask.pickLocation}")
        holder.dropLocation.text = Html.fromHtml("<b>Drop location:</b> ${selectedTask.dropLocation}")
        holder.typeOfGoods.text = Html.fromHtml("<b>Goods:</b> ${selectedTask.typeOfGoods}")
        holder.carBrand.text = Html.fromHtml("<b>Car brand:</b> ${selectedTask.brand}")
        holder.numberPlate.text = Html.fromHtml("<b>Number plate:</b> ${selectedTask.numberPlate}")

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
        private lateinit var progressDialog: ProgressDialog
        init {
            itemView.findViewById<TextView>(R.id.txt_dropLocation).setOnClickListener {
                progressDialog = ProgressDialog(activity)
                progressDialog.setMessage("Please wait...")
                progressDialog.setCancelable(false)
                progressDialog.show()
                itemView.findViewById<TextView>(R.id.txt_pickLocation).isEnabled = false
                itemView.findViewById<TextView>(R.id.txt_dropLocation).isEnabled = false
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedTask = selectedTasksList[position]
                    val address =selectedTask.dropLocation.toString()
                   val result = getAddressLatLng(context, address)

                  if (result != null) {
                       val (latitude, longitude) = result
                      val sharedPref:SharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)

                        val intent:Intent = Intent(activity, MainActivity::class.java)
                        //intent.putExtra("lat",latitude)
                      //  intent.putExtra("long",longitude)
                     //   intent.putExtra("organization",selectedTask.name)
                      val editor = sharedPref.edit()
                      editor.putString("lat", latitude.toString())
                      editor.putString("long", longitude.toString())
                      editor.putString("organization", "Organization: ${selectedTask.name} ##Drop")
                      editor.apply()
                        activity.startActivity(intent)

                       // Toast.makeText(ctx,"Latitude: $latitude, Longitude: $longitude",Toast.LENGTH_LONG).show()

                  } else {
                        // Handle the case where the address could not be geocoded
                  }

                }
            }

            itemView.findViewById<TextView>(R.id.txt_pickLocation).setOnClickListener {
                progressDialog = ProgressDialog(activity)
                progressDialog.setMessage("Please wait...") // Set a message to be displayed
                progressDialog.setCancelable(false)
                progressDialog.show()
                itemView.findViewById<TextView>(R.id.txt_pickLocation).isEnabled = false
                itemView.findViewById<TextView>(R.id.txt_dropLocation).isEnabled = false
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedTask = selectedTasksList[position]
                    val address =selectedTask.pickLocation.toString()
                    val result = getAddressLatLng(context, address)

                    if (result != null) {
                        val (latitude, longitude) = result
                        val sharedPref:SharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)

                        val intent:Intent = Intent(activity, MainActivity::class.java)
                        //intent.putExtra("lat",latitude)
                        //  intent.putExtra("long",longitude)
                        //   intent.putExtra("organization",selectedTask.name)
                        val editor = sharedPref.edit()
                        editor.putString("lat", latitude.toString())
                        editor.putString("long", longitude.toString())
                        editor.putString("organization", "Organization: ${selectedTask.name} ##Pick up")
                        editor.apply()
                        activity.startActivity(intent)

                        // Toast.makeText(ctx,"Latitude: $latitude, Longitude: $longitude",Toast.LENGTH_LONG).show()

                    } else {
                        // Handle the case where the address could not be geocoded
                    }

                }
            }

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

                itemView.findViewById<CircleImageView>(R.id.employeeImageView).setOnClickListener {
                    val positions = adapterPosition
                    if (positions != RecyclerView.NO_POSITION) {
                        val selectedTasks = selectedTasksList[positions]
                   if(selectedTasks.UID != FirebaseAuth.getInstance().uid){
                       val sharedPref: SharedPreferences =
                           activity.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
                       val editor = sharedPref.edit()
                       editor.putString("key", selectedTasks.employeePicture)
                       editor.apply()
                       val intent = Intent(activity, ChatAct::class.java)
                       intent.putExtra("fullName", "${selectedTasks.employeeName} ${selectedTasks.employeeSurname}")
                       // intent.putExtra("pictureUrl", user.pictureUrl)
                       intent.putExtra("id",selectedTasks.UID )
                       activity.startActivity(intent)
                   }else{
                       Toast.makeText(context,"You cannot chat to yourself!!!",Toast.LENGTH_SHORT).show()
                   }

                }
            }

        }
        fun getAddressLatLng(context: Context, address: String): Pair<Double, Double>? {
            val geocoder = Geocoder(context)
            try {
                val addresses: List<Address> = geocoder.getFromLocationName(address, 1)!!
                if (addresses.isNotEmpty()) {
                    val latitude = addresses[0].latitude
                    val longitude = addresses[0].longitude
                    return Pair(latitude, longitude)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return null
        }
    }
}
