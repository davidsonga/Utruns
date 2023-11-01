package com.example.utrun.Fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
        var uid:String =""
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

                            uid = childSnapshot.key.toString()
                            val currentuid = childSnapshot.key.toString()
                            // Reference to the chat node for this user
                            val chatReference = FirebaseDatabase.getInstance().getReference("chats")
                                .child(FirebaseAuth.getInstance().currentUser?.uid + currentuid)


                            // Query to get the last message, sorted by timestamp
                            val query = chatReference.orderByChild("timestamp").limitToLast(1)

                            query.addListenerForSingleValueEvent(object : ValueEventListener {
                                @SuppressLint("NotifyDataSetChanged")
                                override fun onDataChange(messageSnapshot: DataSnapshot) {
                                    if (messageSnapshot.exists()) {
                                        for (messageData in messageSnapshot.children) {
                                            val lastMessage = messageData.child("message").getValue(String::class.java)
                                            lastText[currentuid] = lastMessage.toString()
                                        }
                                    } else {
                                        // Handle the case where there are no messages
                                        lastText[currentuid] = "No messages"
                                    }

                                    // Notify the adapter of the data change
                                    userAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle the error here if needed
                                }
                            })

                            if (!base64Image.isNullOrEmpty() &&uid != FirebaseAuth.getInstance().uid) {
                                userList.add(User(fullName, base64Image, uid  ))

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

                databaseReference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        for (childSnapshot in dataSnapshot.children) {

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
                                            val lastMessage = messageData.child("message")
                                                .getValue(String::class.java)

                                        }
                                    } else {
                                        // Handle the case where there are no messages

                                    }

                                    // Notify the adapter of the data change
                                    userAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    // Handle the error here if needed
                                }
                            })

                        }
                    }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
        return 0
    }


    private fun openChatWithUser(user: User) {

        val sharedPref:SharedPreferences = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("key", user.pictureUrl) // Replace "key" and "value" with your actual data
        editor.apply()
        val intent = Intent(context, ChatAct::class.java)
        intent.putExtra("fullName", user.fullName)
        // intent.putExtra("pictureUrl", user.pictureUrl)
        intent.putExtra("id", user.uid)
        startActivity(intent)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
