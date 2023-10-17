package com.example.utrun.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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

        // Fetch user data and last messages
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
                        userList.add(User(fullName, base64Image, uid))
                    }
                }

                userAdapter.setData(userList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error here if needed
            }
        })

        return view
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
