package com.example.utrun.Adapter

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.R
import com.example.utrun.models.User

class UserAdapter(
    private var userList: List<User>,
    private val itemClickListener: (User) -> Unit,
    private val lastText: Map<String, String> // Map user ID to last text
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_picture, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        // Decode the Base64 string to a Bitmap and set it in the ImageView
        val imageBytes = Base64.decode(user.pictureUrl, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        holder.userImage.setImageBitmap(bitmap)

        // Set the full name in the TextView
        holder.userEmail.text = user.fullName
        holder.messageCount.text= user.index.toString()
        // Get the last message from the lastText map using the user's UID
        val lastMessage = lastText[user.uid]

        // Set the last message in the TextView
        holder.lastMessage.text = lastMessage?.take(30)

        holder.itemView.setOnClickListener {
            itemClickListener(user)
        }
    }

    override fun getItemCount() = userList.size

    fun setData(users: List<User>) {
        userList = users
        notifyDataSetChanged()
    }




    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.user_image)
        val userEmail: TextView = itemView.findViewById(R.id.user_email)
        val lastMessage: TextView = itemView.findViewById(R.id.lastMessage)
        val messageCount:TextView = itemView.findViewById(R.id.messageCount)
    }
}
