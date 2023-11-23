package com.example.utrun.Fragment

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.Activity.ChatAct
import com.example.utrun.Adapter.UserAdapter
import com.example.utrun.databinding.FragmentChatBinding
import com.example.utrun.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.concurrent.atomic.AtomicInteger

class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var chatListener: ValueEventListener? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var databaseReference: DatabaseReference
    private val lastText: MutableMap<String, String> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupChatListener()
        return binding.root
    }

    private fun setupRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        userAdapter = UserAdapter(arrayListOf(), this::openChatWithUser, lastText)
        recyclerView.adapter = userAdapter
    }

    private fun setupChatListener() {
        databaseReference = FirebaseDatabase.getInstance().getReference("login/email")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList = mutableListOf<User>()
                for (childSnapshot in dataSnapshot.children) {
                    val base64Image = childSnapshot.child("Picture").value as String?
                    val name = childSnapshot.child("name").getValue(String::class.java)
                    val surname = childSnapshot.child("surname").getValue(String::class.java)
                    val fullName = "$name $surname"
                    val uid = childSnapshot.key


                    val currentuid = childSnapshot.key.toString()

                    if (!base64Image.isNullOrEmpty() && uid != FirebaseAuth.getInstance().uid) {
                        userList.add(User(fullName, base64Image, uid ?: ""))
                    }
                }
                fetchLastMessagesForUsers(userList) {
                    userAdapter.setData(userList)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error here if needed
            }
        })

    }
    private fun fetchLastMessagesForUsers(userList: MutableList<User>, completion: () -> Unit) {
        val pendingFetchCount = AtomicInteger(userList.size)

        userList.forEach { user ->
            val chatReference = FirebaseDatabase.getInstance().getReference("chats")
                .child(FirebaseAuth.getInstance().currentUser?.uid + user.uid)

            chatReference.orderByChild("timestamp").limitToLast(1)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(messageSnapshot: DataSnapshot) {
                        val lastMessage = if (messageSnapshot.exists()) {
                            messageSnapshot.children.firstOrNull()?.child("message")?.getValue(String::class.java) ?: "No messages"
                        } else {
                            "No messages"
                        }
                        val timestamp = messageSnapshot.children.firstOrNull()?.child("timestamp")?.getValue(Double::class.java) ?: 0.0

                        lastText[user.uid] = lastMessage
                        user.lastMessageTimestamp = timestamp // Update the timestamp here

                        if (pendingFetchCount.decrementAndGet() == 0) {
                            userList.sortByDescending { it.lastMessageTimestamp } // Sort the userList
                            userAdapter.setData(userList) // Update the adapter
                            completion.invoke()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle the error here if needed
                    }
                })
        }
    }


    private fun openChatWithUser(user: User) {
        val sharedPref: SharedPreferences = requireActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("key", user.pictureUrl)
        editor.apply()

        val intent = Intent(context, ChatAct::class.java).apply {
            putExtra("fullName", user.fullName)
            putExtra("id", user.uid)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        chatListener?.let {
            databaseReference.removeEventListener(it)
        }
        _binding = null

    }
}
