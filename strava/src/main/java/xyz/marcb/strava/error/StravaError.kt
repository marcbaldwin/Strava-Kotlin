package xyz.marcb.strava.error

sealed class StravaError {

    // Unexpected error from authenticate
    class AuthUnexpectedError(code: String?) : Error("Unexpected error, code: '${code ?: "None"}'")

    // User denied access to the app from the authenticate screen
    object AuthUserDeniedAccess : Error("User denied access")

    // 400 Bad request
    class BadRequest(response: StravaErrorResponse) :
        Error("Bad request, response: '${response.description}'")

    // 400 Bad request -> Refresh token invalid
    object RefreshTokenInvalid : Error("Refresh token invalid")

    // 401 Unauthorized -> Access token invalid
    object AccessTokenInvalid : Error("Access token invalid")

    // 403 Forbidden (Likely user has not accepted license agreement)
    class Forbidden(response: StravaErrorResponse) :
        Error("Forbidden, response: '${response.description}'")

    class ResourceNotFound(response: StravaErrorResponse) : Error(response.description)

    // 4XX Other
    class ApiUnexpectedError(response: StravaErrorResponse) :
        Error("Unexpected error, response: '${response.description}'")
}
