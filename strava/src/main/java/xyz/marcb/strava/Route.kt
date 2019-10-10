package xyz.marcb.strava

data class Route(
    val id: Long,
    val name: String,
    val description: String?,
    val map: Map,
    val created_at: String,
    val updated_at: String
)
