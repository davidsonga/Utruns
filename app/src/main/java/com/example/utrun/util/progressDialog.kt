package com.example.utrun.util

import android.app.Activity
import android.app.ProgressDialog

class progressDialog {
    private var isTrue:Boolean = false
    private lateinit var progressDialog: ProgressDialog


    fun isProgressDialogEnable(activity:Activity,message:String):Boolean{

        progressDialog = ProgressDialog(activity)
        progressDialog.setMessage("Loading...") // Set a message to be displayed
        progressDialog.setCancelable(false)
        progressDialog.show()
        return isTrue
    }

    fun isProgressDialogDisable():Boolean{

        progressDialog.dismiss()
        return isTrue
    }
}