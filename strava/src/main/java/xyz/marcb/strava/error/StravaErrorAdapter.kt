package xyz.marcb.strava.error

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import retrofit2.HttpException

object StravaErrorAdapter {

    private val responseAdapter: JsonAdapter<StravaErrorResponse> =
        Moshi.Builder().build().adapter(StravaErrorResponse::class.java)

    fun convert(error: Throwable): Throwable {
        return when (error) {
            is HttpException -> convertHttpException(error)
            else -> error
        }
    }

    private fun convertHttpException(error: HttpException): Throwable {
        val response = error.response()?.errorBody()?.string() ?: return error
        try {
            val errorResponse = responseAdapter.fromJson(response) ?: return error
            return convert(errorResponse, error)
        } catch (jsonDecodingError: Throwable) {
            return error
        }
    }

    private fun convert(response: StravaErrorResponse, error: HttpException): Error {
        return when (error.code()) {
            400 -> {
                val isNotAuthorized = response.errors?.firstOrNull {
                    it.resource == StravaErrorResponse.refreshToken
                } != null
                if (isNotAuthorized) StravaError.RefreshTokenInvalid
                else StravaError.BadRequest(response)
            }
            401 -> StravaError.AccessTokenInvalid
            403 -> StravaError.Forbidden(response)
            else -> StravaError.ApiUnexpectedError(response)
        }
    }
}
