package com.example.utrun.util

import android.app.Activity
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity

class intents {

    fun intent(activity1: Activity,activity2: Activity){
        var intent:Intent = Intent(activity1,activity2::class.java)
        activity1.startActivity(intent)
    }


}