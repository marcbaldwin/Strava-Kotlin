package xyz.marcb.strava

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Map(
    val id: String,
    val summary_polyline: String?
)
