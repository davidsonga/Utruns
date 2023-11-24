package com.example.utrun.models

data class Task(
    var assignedTimestamp: Long = 0L,
    var completedTimestamp: Long = 0L,
    var dropoffLocationId: String? = null,
    var employeeUid: String? = null,
    var pickupLocation: String? = null,
    var ratingId: String? = null,
    var status: String? = null,
    var type: String? = null,
    var typeOfGoods: String? = null,
    var uploadedTimestamp: Long = 0L,
    var vehicleId: String? = null,
    var dropoffLocation: String? = null,
    var vehicleBrand: String? = null, // Optional field
    var vehicleNumberPlate: String? = null // Optional field
)
