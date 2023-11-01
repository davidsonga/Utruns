package com.example.utrun.Activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.example.utrun.MainActivity
import com.example.utrun.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Rate : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate)

        val arrayString = arrayOf("Punctuality", "0", "1", "2", "3", "4", "5")
        val arrayString1 = arrayOf( "Politeness", "0", "1", "2", "3", "4", "5")
        val arrayString2 = arrayOf( "Productivity", "0", "1", "2", "3", "4", "5")
        val arrayString3 = arrayOf(  "Product Handling", "0", "1", "2", "3", "4", "5")
        val arrayString4 = arrayOf(  "Process and Efficiency","0", "1", "2", "3", "4", "5")


        val spinner1 = findViewById<Spinner>(R.id.spinner1)
        val spinner2 = findViewById<Spinner>(R.id.spinner2)
        val spinner3 = findViewById<Spinner>(R.id.spinner3)
        val spinner4 = findViewById<Spinner>(R.id.spinner4)
        val spinner5 = findViewById<Spinner>(R.id.spinner5)

// Create separate ArrayAdapter instances for each Spinner
        val adapter1 = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayString)
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayString1)
        val adapter3 = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayString2)
        val adapter4 = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayString3)
        val adapter5 = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayString4)

        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner1.adapter = adapter1
        spinner2.adapter = adapter2
        spinner3.adapter = adapter3
        spinner4.adapter = adapter4
        spinner5.adapter = adapter5

// Set prompts for each Spinner
        spinner1.prompt = "Punctuality"
        spinner2.prompt = "Politeness"
        spinner3.prompt = "Productivity"
        spinner4.prompt = "Product Handling"
        spinner5.prompt = "Process and Efficiency"

// Set the gravity for each Spinner
        spinner1.gravity = android.view.Gravity.CENTER
        spinner2.gravity = android.view.Gravity.CENTER
        spinner3.gravity = android.view.Gravity.CENTER
        spinner4.gravity = android.view.Gravity.CENTER
        spinner5.gravity = android.view.Gravity.CENTER


        val btn = findViewById<Button>(R.id.btn_finishOnboarding)

        btn.setOnClickListener() {
            val spinner1Value = spinner1.selectedItem.toString()
            val spinner2Value = spinner2.selectedItem.toString()
            val spinner3Value = spinner3.selectedItem.toString()
            val spinner4Value = spinner4.selectedItem.toString()
            val spinner5Value = spinner5.selectedItem.toString()
            val selectedValues = arrayOf(spinner1Value, spinner2Value, spinner3Value, spinner4Value, spinner5Value)
            val array = arrayOf("Punctuality", "Politeness", "Productivity", "Product Handling", "Process and Efficiency")

            val matchingValues = mutableListOf<String>()

            for (selectedValue in selectedValues) {
                if (selectedValue in array) {
                    matchingValues.add(selectedValue)
                }
            }

            if (matchingValues.isNotEmpty()) {
                val message = "You cannot select the following: ${matchingValues.joinToString(", ")}"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            } else {

                val taskCode = intent.getStringExtra("taskID") ?: ""
                val currentTimeMillis = System.currentTimeMillis()
               val uploadRate= FirebaseDatabase.getInstance().reference.child("Rate").child(taskCode).child(FirebaseAuth.getInstance().uid.toString())
               uploadRate.child("Punctuality").setValue(spinner1Value)
               uploadRate.child("Politeness").setValue(spinner2Value)
               uploadRate.child("Productivity").setValue(spinner3Value)
               uploadRate.child("Product Handling").setValue(spinner4Value)
               uploadRate.child("Process and Efficiency").setValue(spinner5Value)
               uploadRate.child("Rate time").setValue(currentTimeMillis)

                Toast.makeText(this,"Rate success",Toast.LENGTH_LONG).show()

                val intent:Intent = Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()



            }




        }

    }
}
