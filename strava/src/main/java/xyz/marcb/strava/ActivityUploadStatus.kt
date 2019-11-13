package xyz.marcb.strava

data class ActivityUploadStatus(
    val id: Long,
    val external_id: String?,
    val error: String?,
    val status: String,
    val activity_id: Long?
)
