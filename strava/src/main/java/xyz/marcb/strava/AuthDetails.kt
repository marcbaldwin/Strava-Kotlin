package xyz.marcb.strava

import java.util.concurrent.TimeUnit
import kotlinx.serialization.Serializable

@Serializable
data class AuthDetails(
    val refreshToken: String,
    val accessToken: String,
    val accessTokenExpiry: Long
) {

    val hasExpired: Boolean
        get() = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) >= accessTokenExpiry
}
