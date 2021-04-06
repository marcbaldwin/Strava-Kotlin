package xyz.marcb.strava.error

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StravaErrorResponse(
    val message: String?,
    val errors: List<Error>?
) {

    @JsonClass(generateAdapter = true)
    data class Error(
        val resource: String?,
        val field: String?,
        val code: String?
    ) {

        val description: String
            get() = listOfNotNull(resource, this.field, code).joinToString(" ")
    }

    val description: String
        get() = (
            listOfNotNull(message) + errors.orEmpty()
                .map { it.description }
            ).joinToString(", ")

    companion object {

        // Messages
        const val badRequest = "Bad Request"
        const val authorizationError = "Authorization Error"

        // Resources
        const val refreshToken = "RefreshToken"

        // Fields
        const val code = "code"
        const val accessToken = "access_token"

        // Codes
        const val invalid = "invalid"
    }
}
