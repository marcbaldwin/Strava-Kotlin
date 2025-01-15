package xyz.marcb.strava

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class AuthDetails(
    val refreshToken: String,
    val accessToken: String,
    val accessTokenExpiry: Long
) {

    val hasExpired: Boolean
        get() = Clock.System.now().epochSeconds >= accessTokenExpiry
}
