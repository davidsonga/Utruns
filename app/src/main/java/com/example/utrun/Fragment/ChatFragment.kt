package com.example.utrun.Fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utrun.Adapter.UserAdapter
import com.example.utrun.databinding.FragmentChatBinding
import com.example.utrun.models.message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.TreeMap


class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private var chatListener: ValueEventListener? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var chatReference: DatabaseReference
    private val userList = mutableListOf<message>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
       userAdapter = UserAdapter(userList, requireContext())
        setupRecyclerView()
        setupChatListener()

        binding.sendMessage.setOnClickListener(){
            if(binding.messageEd.text.toString() != ""){
                sendMessage(binding.messageEd.text.toString())
            }
        }

        binding.messageEd.addTextChangedListener(object: TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                binding.recycler.scrollToPosition(userAdapter.itemCount - 1)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.recycler.scrollToPosition(userAdapter.itemCount - 1)
            }

            override fun afterTextChanged(s: Editable?) {
                binding.recycler.scrollToPosition(userAdapter.itemCount - 1)
            }

        })
        binding.recycler.scrollToPosition(userAdapter.itemCount - 1)
        return binding.root
    }

    private fun sendMessage(text: String?) {
   val database = FirebaseDatabase.getInstance().reference.child("login").child("email").child(FirebaseAuth.getInstance().uid.toString())

// Set the time zone to South Africa
        val tz = TimeZone.getTimeZone("Africa/Johannesburg")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = tz

// Get the current timestamp in South Africa
        val timestampString = sdf.format(Date())
        var timestamp: Long = 0

        try {
            val date = sdf.parse(timestampString)
            timestamp = date?.time ?: 0L // Get the timestamp in milliseconds or 0 if null
        } catch (e: ParseException) {
            // Handle the error as needed, e.g., log the error or set a default value
            timestamp = 0L
        }

       database.addValueEventListener(object :ValueEventListener{
           override fun onDataChange(snapshot: DataSnapshot) {
              val picture = snapshot.child("Picture").getValue(String::class.java)?:""
               val name = snapshot.child("name").getValue(String::class.java)?:""
               val surname= snapshot.child("surname").getValue(String::class.java)?:""
               val fullname:String = "$name $surname"

               if(picture.isNotEmpty() &&picture.isNotBlank() ){
                   val chatMessages =FirebaseDatabase.getInstance().reference.child("chats").child(timestamp.toString()).child(FirebaseAuth.getInstance().uid.toString())

                   val valueMap = hashMapOf(
                       "message" to text,
                       "fullname" to fullname)

                   chatMessages.setValue(valueMap)
                       .addOnSuccessListener {
                           binding.messageEd.text.clear()

                       }

               }else{
                   Toast.makeText(requireContext(),"You need a profile picture to send messages!!!",Toast.LENGTH_LONG).show()
               }



           }

           override fun onCancelled(error: DatabaseError) {

           }

       })

    }

    private fun setupRecyclerView() {
        recyclerView = binding.recycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter if it hasn't been initialized yet
        if (!::userAdapter.isInitialized) {
            userAdapter = UserAdapter(userList, requireContext())
        }

        recyclerView.adapter = userAdapter
    }

    private fun setupChatListener() {
        databaseReference = FirebaseDatabase.getInstance().reference.child("login").child("email")
        chatReference = FirebaseDatabase.getInstance().reference.child("chats")

        chatReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sortedMessages = TreeMap<Long, message>()

                snapshot.children.forEach { chatSnapshot ->
                    val timespan = chatSnapshot.key?.toLongOrNull() ?: return@forEach
                    chatSnapshot.children.forEach { childChatSnapshot ->
                        val uid = childChatSnapshot.key ?: return@forEach
                        val chat = childChatSnapshot.child("message").getValue(String::class.java) ?: return@forEach

                        databaseReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val picture = userSnapshot.child("Picture").getValue(String::class.java) ?: ""
                                val name = userSnapshot.child("name").getValue(String::class.java) ?: ""
                                val surname = userSnapshot.child("surname").getValue(String::class.java) ?: ""
                                val fullname = "$name $surname"

                                val objChat = message(uid, timespan.toString(), chat, picture, fullname)
                                if (picture.isNotEmpty() && chat.isNotEmpty() && fullname.isNotEmpty()) {
                                    sortedMessages[timespan] = objChat
                                    val sortedList = sortedMessages.values.toList()
                                    userAdapter.setData(sortedList)
                                    binding.recycler.scrollToPosition(userAdapter.itemCount - 1)
                                }
                            }

                            override fun onCancelled(userError: DatabaseError) {
                                // Handle error
                            }
                        })
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }








    override fun onDestroyView() {
        super.onDestroyView()
        chatListener?.let {
            databaseReference.removeEventListener(it)
        }
        _binding = null

    }
}
