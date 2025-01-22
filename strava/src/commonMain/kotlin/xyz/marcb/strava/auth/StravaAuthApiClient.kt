package xyz.marcb.strava.auth

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.post
import io.ktor.http.ParametersBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import xyz.marcb.strava.AuthDetails
import xyz.marcb.strava.error.StravaError
import xyz.marcb.strava.error.StravaErrorAdapter
import xyz.marcb.strava.error.StravaErrorResponse

class StravaAuthApiClient(
    private val clientId: String,
    private val clientSecret: String,
    enableLogging: Boolean = false,
) {

    var onAuthDetailsRefreshed: ((AuthDetails) -> Unit)? = null

    companion object {
        private const val BASE_URL = "https://www.strava.com"
    }

    private val json = Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }
        if (enableLogging) {
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }
        }
        defaultRequest {
            url(BASE_URL)
            url {
                parameters.apply {
                    append("client_id", clientId)
                    append("client_secret", clientSecret)
                }
            }
        }
    }

    fun authorizeUri(redirectUrl: String, scopes: Set<String>): String {
        return "$BASE_URL/oauth/mobile/authorize" +
                "?client_id=$clientId" +
                "&redirect_uri=$redirectUrl" +
                "&response_type=code" +
                "&approval_prompt=force" +
                "&scope=${scopes.joinToString(",")}"
    }

    suspend fun authorize(uriQueryParameter: (String) -> String?): StravaAuthResponse {
        val code = uriQueryParameter("code")

        if (code != null) {
            return request(path = "/oauth/token") {
                append("code", code)
                append("grant_type", "authorization_code")
            }
        } else {
            throw when (val value = uriQueryParameter("error")) {
                "access_denied" -> StravaError.AuthUserDeniedAccess
                else -> StravaError.AuthUnexpectedError(value)
            }
        }
    }

    fun scopes(uriQueryParameter: (String) -> String?): List<String>? {
        return uriQueryParameter("scope")?.split(",")
    }

    suspend fun accessToken(authDetails: AuthDetails): String {
        return when (authDetails.hasExpired) {
            true -> refreshAccessToken(authDetails.refreshToken)
            false -> authDetails.accessToken
        }
    }

    suspend fun refreshAccessToken(refreshToken: String): String {
        val response = request<StravaAuthRefreshTokenResponse>(
            path = "/oauth/token"
        ) {
            append("refresh_token", refreshToken)
            append("grant_type", "refresh_token")
        }
        onAuthDetailsRefreshed?.invoke(response.authDetails)

        return response.access_token
    }

    @Throws(Throwable::class)
    private suspend inline fun <reified T> request(
        path: String,
        crossinline request: ParametersBuilder.() -> Unit,
    ): T {
        return withContext(Dispatchers.IO) {
            val call = client.post(path) {
                url { parameters.request() }
            }.call
            request<T>(call)
        }
    }

    @Suppress("MagicNumber")
    private suspend inline fun <reified T> request(call: HttpClientCall): T {
        if (call.response.status.value in 200..299) {
            return call.body<T>()
        } else {
            val errorResponse = call.body<StravaErrorResponse>()
            val error = StravaErrorAdapter.convert(errorResponse, call.response.status.value)
            throw error
        }
    }
}
