package com.example.utrun.models

data class User(
    val fullName: String,
    val pictureUrl: String,
    val uid: String,
    var lastMessageTimestamp: Double = 0.0 // Add this line
)
