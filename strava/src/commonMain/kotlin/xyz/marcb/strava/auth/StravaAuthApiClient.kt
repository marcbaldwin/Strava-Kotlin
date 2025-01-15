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
import xyz.marcb.strava.uri.UriBuilder
import xyz.marcb.strava.uri.WrappedUri
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

    fun authorizeUri(uriBuilder: UriBuilder, redirectUrl: String, scopes: Set<String>): UriBuilder {
        return uriBuilder.setPath("$BASE_URL/oauth/mobile/authorize")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUrl)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "force")
            .appendQueryParameter("scope", scopes.joinToString(","))
    }

    suspend fun authorize(uri: WrappedUri): StravaAuthResponse {
        val code = uri.getQueryParameter("code")

        if (code != null) {
            return request(path = "/oauth/token?grant_type=authorization_code") {
                append("code", code)
            }
        } else {
            throw when (val value = uri.getQueryParameter("error")) {
                "access_denied" -> StravaError.AuthUserDeniedAccess
                else -> StravaError.AuthUnexpectedError(value)
            }
        }
    }

    fun scopes(uri: WrappedUri): List<String>? {
        return uri.getQueryParameter("scope")?.split(",")
    }

    suspend fun accessToken(authDetails: AuthDetails): String {
        return when (authDetails.hasExpired) {
            true -> refreshAccessToken(authDetails.refreshToken)
            false -> authDetails.accessToken
        }
    }

    suspend fun refreshAccessToken(refreshToken: String): String {
        val response = request<StravaAuthRefreshTokenResponse>(
            path = "/oauth/token?grant_type=refresh_token"
        ) {
            append("refresh_token", refreshToken)
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
