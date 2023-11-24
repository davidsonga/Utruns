package com.example.utrun.models

import android.widget.TextView
import com.example.utrun.R

data class Tasks(
         var userID: String = "",
         var dropLocation: String = "",
         var placeNames: String = "",
         var pickLocation: String = "",
         var time: String = "",
         var typeOfGoods: String = "",
         var txt_types: String = "",
         var uniqueID: String? = ""
)

