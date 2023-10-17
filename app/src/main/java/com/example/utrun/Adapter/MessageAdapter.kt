package com.example.utrun.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.R
import com.example.utrun.models.MessageModel
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(context: Context) : RecyclerView.Adapter<MessageAdapter.MyViewHolder>() {

    private lateinit var context: Context
    private var messageModelList: List<MessageModel> = ArrayList()


    init {
        this.context = context
    }

    fun add(messageModel: MessageModel) {
        val newList = ArrayList(messageModelList)
        newList.add(messageModel)
        messageModelList = newList
        notifyDataSetChanged()
    }

    fun clear() {
        messageModelList = emptyList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_row, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message: MessageModel = messageModelList[position]
        holder.msg.text = message.message.trim();

        if (message.senderId == FirebaseAuth.getInstance().uid) {
            // Sender's message
            holder.msg.text = message.message.trim();
            holder.msg2.visibility = View.GONE


        } else {
            // Receiver's message
            holder.msg.visibility = View.GONE
            holder.msg2.text = message.message.trim();



        }
    }



    override fun getItemCount(): Int {
        return messageModelList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var msg: TextView = itemView.findViewById(R.id.message)
        var msg2: TextView = itemView.findViewById(R.id.message2)

        var main: LinearLayout = itemView.findViewById(R.id.mainMessageLayout)
    }
}
