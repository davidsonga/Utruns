package com.example.utrun.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import com.example.utrun.R

class PlanUserPageDay : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plan_user_page_day)

        val items = listOf("Material", "Design", "Components", "Android")
        val adapter = ArrayAdapter(this, R.layout.list_cars, items)

        val autoCompleteTextView = findViewById<AutoCompleteTextView>(R.id.textField)
        autoCompleteTextView.setAdapter(adapter)
    }
}
