package com.example.utrun.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.Activity.ChatAct
import com.example.utrun.Adapter.UserAdapter
import com.example.utrun.databinding.FragmentChatBinding
import com.example.utrun.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var databaseReference: DatabaseReference
    private var lastText: MutableMap<String, String> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        val view = binding.root
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize userAdapter with an empty list and the lastText map
        userAdapter = UserAdapter(emptyList(), this::openChatWithUser, lastText)
        recyclerView.adapter = userAdapter

        databaseReference = FirebaseDatabase.getInstance().reference.child("login").child("email")
        val handler = Handler()
        val delay = 1000 // 1 second

        val fetchDataRunnable = object : Runnable {
            override fun run() {
                databaseReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userList = mutableListOf<User>()

                        for (childSnapshot in dataSnapshot.children) {
                            val base64Image = childSnapshot.child("Picture").value as String?
                            val name = childSnapshot.child("name").getValue(String::class.java)
                            val surname = childSnapshot.child("surname").getValue(String::class.java)
                            val fullName = "$name $surname"
                            val uid = childSnapshot.key.toString()
                            // Reference to the chat node for this user
                            val chatReference = FirebaseDatabase.getInstance().getReference("chats")
                                .child(FirebaseAuth.getInstance().currentUser?.uid + uid)

                            // Query to get the last message, sorted by timestamp
                            val query = chatReference.orderByChild("timestamp").limitToLast(1)

                            query.addListenerForSingleValueEvent(object : ValueEventListener {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onDataChange(messageSnapshot: DataSnapshot) {
                                    if (messageSnapshot.exists()) {
                                        for (messageData in messageSnapshot.children) {
                                            val lastMessage = messageData.child("message").getValue(String::class.java)
                                            lastText[uid] = lastMessage.toString()
                                        }
                                    } else {
                                        // Handle the case where there are no messages
                                        lastText[uid] = "No messages"
                                    }

                                    // Notify the adapter of the data change
                                    userAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle the error here if needed
                                }
                            })

                            if (!base64Image.isNullOrEmpty()) {
                                userList.add(User(fullName, base64Image, uid, unReadMessageCount(uid)))
                            }
                        }

                        userAdapter.setData(userList)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle error here if needed
                    }
                })


                handler.postDelayed(this, delay.toLong())
            }
        }

// Call the runnable to start fetching data every 1 second
        handler.postDelayed(fetchDataRunnable, delay.toLong())
        // Fetch user data and last messages


        return view
    }


    private fun unReadMessageCount(strangerUID: String): Int {
        val databaseReceiverMessageRead = FirebaseDatabase.getInstance().getReference()
            .child("chats")
            .child("Qy8Ub4g9okWMIFl5ZeuCXG9cRTA3ie49nb9lSmY5rHhFSapxVyRRDMJ3")
            .child("02600625-c8e5-49ef-8f50-1a8457db5eb9")
        var index = 0 // Counter for "no" values

        databaseReceiverMessageRead.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val currentReadMessage = dataSnapshot.child("receiverReadMessage").value.toString()

                    if (currentReadMessage == "No") { // Check for false instead of "No"
                        index++
                    }
                }

                // Now, 'index' contains the number of "no" values in 'receiverMessageRead'
                // You can use this count as needed
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
        return index
    }


    private fun openChatWithUser(user: User) {
        val intent = Intent(context, ChatAct::class.java)
        intent.putExtra("fullName", user.fullName)
        intent.putExtra("pictureUrl", user.pictureUrl)
        intent.putExtra("id", user.uid)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
