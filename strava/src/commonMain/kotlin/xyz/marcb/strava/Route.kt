package xyz.marcb.strava

import kotlinx.serialization.Serializable

@Serializable
data class Route(
    val id: Long,
    val name: String,
    val description: String?,
    val map: Map,
    val created_at: String,
    val updated_at: String
)
