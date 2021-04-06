package xyz.marcb.strava

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ActivityUploadStatus(
    val id: Long,
    val external_id: String?,
    val error: String?,
    val status: String,
    val activity_id: Long?
)
