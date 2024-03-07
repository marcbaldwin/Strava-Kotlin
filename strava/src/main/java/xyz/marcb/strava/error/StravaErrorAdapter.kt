@file:OptIn(ExperimentalSerializationApi::class)

package xyz.marcb.strava.error

import java.io.IOException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import retrofit2.HttpException

object StravaErrorAdapter {

    var onDecodingError: ((IOException) -> Unit)? = null

    private val json = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    fun convert(error: Throwable): Throwable {
        return when (error) {
            is HttpException -> convertHttpException(error)
            else -> error
        }
    }

    fun convert(response: String, httpCode: Int): Throwable? {
        return try {
            val errorResponse = json.decodeFromString<StravaErrorResponse>(response)
            convert(errorResponse, httpCode)
        } catch (jsonDecodingError: IOException) {
            onDecodingError?.invoke(jsonDecodingError)
            null
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
