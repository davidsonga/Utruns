package com.example.utrun.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.Activity.SelectCar
import com.example.utrun.R
import com.example.utrun.models.Tasks
import com.example.utrun.models.User
import com.example.utrun.models.message
import com.example.utrun.models.timespan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserAdapter(
    private var userList: List<message>,
    private val context: Context

) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_row, parent, false)

        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        val currentUid = FirebaseAuth.getInstance().uid.toString()



      if(user.uid == currentUid){
          // Decode the Base64 string to a Bitmap and set it in the ImageView
          val imageBytes = Base64.decode(user.picture, Base64.DEFAULT)
          val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)


          holder.imgSender.setImageBitmap(bitmap)
          holder.message2.text = user.txtMessage.trim()
          holder.imgSender.visibility = View.VISIBLE
          holder.message2.visibility = View.VISIBLE
          holder.imgReceiver.visibility = View.GONE
          holder.message.visibility = View.GONE
      }
        else{
          // Decode the Base64 string to a Bitmap and set it in the ImageView
          val imageBytes = Base64.decode(user.picture, Base64.DEFAULT)
          val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
          holder.imgReceiver.setImageBitmap(bitmap)
          holder.message.text = user.txtMessage.trim()

          holder.imgReceiver.visibility = View.VISIBLE
          holder.message.visibility = View.VISIBLE
          holder.imgSender.visibility = View.GONE
          holder.message2.visibility = View.GONE
      }


    }

    override fun getItemCount() = userList.size

    fun setData(users: List<message>) {
        userList = users
        notifyDataSetChanged()
    }




    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgReceiver: ImageView = itemView.findViewById(R.id.imgReceiver)
        val imgSender: ImageView = itemView.findViewById(R.id.imgSender)
        val message2: TextView = itemView.findViewById(R.id.message2)
        val message: TextView = itemView.findViewById(R.id.message)

        init {
            imgSender.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedImage = userList[position]

                    if(FirebaseAuth.getInstance().uid.toString() == selectedImage.uid){
                        Toast.makeText(context, "name: ${selectedImage.fullName}",Toast.LENGTH_LONG).show()
                    }


                }


            }
        }

        init {
            imgReceiver.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val selectedImage = userList[position]

                    if(FirebaseAuth.getInstance().uid.toString() != selectedImage.uid){
                        Toast.makeText(context, "name: ${selectedImage.fullName}",Toast.LENGTH_LONG).show()
                    }


                }


            }
        }
    }
}
