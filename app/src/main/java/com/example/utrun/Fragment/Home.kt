package com.example.utrun.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.utrun.R
import com.example.utrun.Service.AppStateService
import com.google.firebase.auth.FirebaseAuth


class Home : Fragment()  {


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view:View =inflater.inflate(R.layout.fragment_home, container, false)



        var btn = view.findViewById<Button>(R.id.btn)

        btn.setOnClickListener(){
            FirebaseAuth.getInstance().signOut()
            //set user offline
            val serviceIntent = Intent(requireContext(), AppStateService::class.java)
            requireContext().stopService(serviceIntent)
            //send user back to login
            var intent:Intent= Intent(requireContext(),com.example.utrun.MainActivity::class.java)
            startActivity(intent)
        }

        val serviceIntent = Intent(requireContext(), AppStateService::class.java)
         requireContext().startService(serviceIntent)

        // Inflate the layout for this fragment
        return view
    }



}