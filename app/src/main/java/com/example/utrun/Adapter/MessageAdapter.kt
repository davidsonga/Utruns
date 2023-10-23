package com.example.utrun.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.R
import com.example.utrun.models.MessageModel
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    override fun getItemViewType(position: Int): Int {
        val message = messageModelList[position]
        return if (message.senderId == FirebaseAuth.getInstance().uid) {
            // Sender's message
            0
        } else {
            // Receiver's message
            1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
      return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sender_message_row, parent, false)
            SenderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.receiver_message_row, parent, false)
            ReceiverViewHolder(view)
        }

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message: MessageModel = messageModelList[position]
        if (holder is SenderViewHolder) {
            holder.msg.text = message.message.trim()
            if(message.currentReadMessage && message.receiverReadMessage.equals("Yes")){
                holder.ivMessageIcon.setImageResource(R.drawable.arrived_read)

            }
            if(!message.currentReadMessage){
                holder.ivMessageIcon.setImageResource(R.drawable.not_arrived)
            }
            if(message.currentReadMessage && message.receiverReadMessage.equals("No")){
                holder.ivMessageIcon.setImageResource(R.drawable.arrived_not_read)
            }
        } else if (holder is ReceiverViewHolder) {
        holder.msg2.text = message.message.trim()

        }
    }

    override fun getItemCount(): Int {
        return messageModelList.size
    }

    inner class SenderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var msg: TextView = itemView.findViewById(R.id.message)
        var ivMessageIcon:ImageView = itemView.findViewById(R.id.ivMessageIcon)
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var msg2: TextView = itemView.findViewById(R.id.message2)

    }
}
