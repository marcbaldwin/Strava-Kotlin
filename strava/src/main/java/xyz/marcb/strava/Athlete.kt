package xyz.marcb.strava

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Athlete(
    val id: String,
    val firstname: String?,
    val lastname: String?,
    val country: String?,
    val measurement_preference: String? // Detailed representation only
)
