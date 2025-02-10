package com.example.traveljourneytracker

data class RouteStop(
    val name: String,            // Name of the stop
    val distance: Int,           // Distance to the next stop
    val visaRequirement: String, // Visa requirement for this stop
    var distanceCovered: Int     // Distance covered so far (dynamic)
)
