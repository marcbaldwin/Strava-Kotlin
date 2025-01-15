package xyz.marcb.strava.error

sealed class StravaError(message: String): Throwable(message) {

    // Unexpected error from authenticate
    class AuthUnexpectedError(code: String?) : StravaError(
        "Unexpected error, code: '${code ?: "None"}'"
    )

    // User denied access to the app from the authenticate screen
    data object AuthUserDeniedAccess : StravaError(
        "User denied access"
    ) {
        private fun readResolve(): Any = AuthUserDeniedAccess
    }

    // 400 Bad request
    class BadRequest(response: StravaErrorResponse) : StravaError(
        "Bad request, response: '${response.description}'"
    )

    // 400 Bad request -> Refresh token invalid
    data object RefreshTokenInvalid : StravaError(
        "Refresh token invalid"
    ) {
        private fun readResolve(): Any = RefreshTokenInvalid
    }

    // 401 Unauthorized -> Access token invalid
    data object AccessTokenInvalid : StravaError(
        "Access token invalid"
    ) {
        private fun readResolve(): Any = AccessTokenInvalid
    }

    // 403 Forbidden (Likely user has not accepted license agreement)
    class Forbidden(response: StravaErrorResponse) : StravaError(
        "Forbidden, response: '${response.description}'"
    )

    class ResourceNotFound(response: StravaErrorResponse) : StravaError(
        response.description
    )

    // 4XX Other
    class ApiUnexpectedError(response: StravaErrorResponse) : StravaError(
        "Unexpected error, response: '${response.description}'"
    )
}
