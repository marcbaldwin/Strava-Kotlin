package xyz.marcb.strava

import kotlinx.serialization.Serializable

@Serializable
data class Map(
    val id: String,
    val summary_polyline: String?
)
