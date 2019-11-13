package xyz.marcb.strava

data class Activity(
    val id: String,
    val name: String,
    val description: String,
    val distance: Float,
    val elapsed_time: Long,
    val moving_time: Long,
    val total_elevation_gain: Float,
    val type: String,
    val start_date: String,
    val map: Map,
    val commute: Boolean,
    val average_speed: Float,
    val max_speed: Float
)
