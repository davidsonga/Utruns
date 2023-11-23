package com.example.utrun.models

data class Task(
    val pickupLocation: String = "",
    val dropoffLocation: String = "",
    val typeOfGoods: String = "",
    val type: String = "",
    val employeeUid: String = "" // UID of the logged-in user
)