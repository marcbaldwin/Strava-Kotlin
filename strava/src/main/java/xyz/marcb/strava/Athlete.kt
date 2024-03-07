package xyz.marcb.strava

import kotlinx.serialization.Serializable

@Serializable
data class Athlete(
    val id: String,
    val firstname: String?,
    val lastname: String?,
    val country: String?,
    val measurement_preference: String? // Detailed representation only
)
