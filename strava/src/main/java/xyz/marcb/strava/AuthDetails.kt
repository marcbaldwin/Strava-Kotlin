package xyz.marcb.strava

import com.squareup.moshi.JsonClass
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class AuthDetails(
    val refreshToken: String,
    val accessToken: String,
    val accessTokenExpiry: Long
) {

    val hasExpired: Boolean
        get() = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) >= accessTokenExpiry
}
