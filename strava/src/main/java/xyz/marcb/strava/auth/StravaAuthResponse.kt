package xyz.marcb.strava.auth

import xyz.marcb.strava.Athlete
import xyz.marcb.strava.AuthDetails

data class StravaAuthResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_at: Long,
    val athlete: Athlete
)

data class StravaAuthRefreshTokenResponse(
    val access_token: String,
    val refresh_token: String,
    val expires_at: Long
) {

    val authDetails: AuthDetails
        get() = AuthDetails(refresh_token, access_token, expires_at)
}
