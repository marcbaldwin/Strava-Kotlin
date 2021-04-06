package xyz.marcb.strava.auth

import com.squareup.moshi.JsonClass
import xyz.marcb.strava.Athlete
import xyz.marcb.strava.AuthDetails

@JsonClass(generateAdapter = true)
data class StravaAuthResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_at: Long,
    val athlete: Athlete
)

@JsonClass(generateAdapter = true)
data class StravaAuthRefreshTokenResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_at: Long
) {

    val authDetails: AuthDetails
        get() = AuthDetails(refresh_token, access_token, expires_at)
}
