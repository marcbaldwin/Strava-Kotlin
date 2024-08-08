package xyz.marcb.strava.error

import java.io.IOException

object StravaErrorAdapter {

    var onDecodingError: ((IOException) -> Unit)? = null

    fun convert(response: StravaErrorResponse, httpCode: Int): StravaError {
        return when (httpCode) {
            400 -> {
                val isNotAuthorized = response.errors?.firstOrNull {
                    it.resource == StravaErrorResponse.refreshToken
                } != null
                if (isNotAuthorized) StravaError.RefreshTokenInvalid
                else StravaError.BadRequest(response)
            }
            401 -> StravaError.AccessTokenInvalid
            403 -> StravaError.Forbidden(response)
            404 -> StravaError.ResourceNotFound(response)
            else -> StravaError.ApiUnexpectedError(response)
        }
    }
}
