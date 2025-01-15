package xyz.marcb.strava

import kotlinx.serialization.Serializable

@Serializable
data class ActivityUploadStatus(
    val id: Long,
    val external_id: String?,
    val error: String?,
    val status: String,
    val activity_id: Long?
)
