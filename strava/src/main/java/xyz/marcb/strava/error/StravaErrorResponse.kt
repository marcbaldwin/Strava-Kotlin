package xyz.marcb.strava.error

data class StravaErrorResponse(
        val message: String?,
        val errors: List<Error>
) {

    data class Error(
            val resource: String?,
            val field: String?,
            val code: String?
    ) {

        val description: String
            get() = listOfNotNull(resource, this.field, code).joinToString(" ")
    }

    val description: String
        get() = (listOfNotNull(message) + errors.map { it.description }).joinToString(", ")

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
