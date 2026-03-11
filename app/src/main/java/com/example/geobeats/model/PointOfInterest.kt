package com.example.geobeats.model

data class PointOfInterest(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val spotifyUri: String,
    val triggerRadiusMeters: Double = 50.0 // Radio en metros para que suene la música
)