package xyz.marcb.strava.error

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.io.IOException
import retrofit2.HttpException

object StravaErrorAdapter {

    var onDecodingError: ((IOException) -> Unit)? = null

    private val responseAdapter: JsonAdapter<StravaErrorResponse> =
        Moshi.Builder().build().adapter(StravaErrorResponse::class.java)

    fun convert(error: Throwable): Throwable {
        return when (error) {
            is HttpException -> convertHttpException(error)
            else -> error
        }
    }

    fun convert(response: String, httpCode: Int): Throwable? {
        try {
            val errorResponse = responseAdapter.fromJson(response) ?: return null
            return convert(errorResponse, httpCode)
        } catch (jsonDecodingError: IOException) {
            onDecodingError?.invoke(jsonDecodingError)
            return null
        }
    }

    private fun convertHttpException(error: HttpException): Throwable {
        val response = error.response()?.errorBody()?.string() ?: return error
        return convert(response, error.code()) ?: error
    }

    private fun convert(response: StravaErrorResponse, httpCode: Int): Error {
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
