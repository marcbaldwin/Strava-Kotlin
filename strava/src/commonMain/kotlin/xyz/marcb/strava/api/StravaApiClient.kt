package xyz.marcb.strava.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.http.ParametersBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import xyz.marcb.strava.Activity
import xyz.marcb.strava.Athlete
import xyz.marcb.strava.AuthDetails
import xyz.marcb.strava.Route
import xyz.marcb.strava.auth.StravaAuthApiClient
import xyz.marcb.strava.error.StravaError
import xyz.marcb.strava.request

class StravaApiClient(
    private val stravaAuthApiClient: StravaAuthApiClient,
    enableLogging: Boolean = false,
) {
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
            url("https://www.strava.com/api/v3/")
        }
    }

    private val errorSubject = MutableSharedFlow<Throwable>()

    val errors: Flow<Throwable>
        get() = errorSubject

    suspend fun athlete(
        authDetails: AuthDetails,
    ): Athlete {
        return request(path = "athlete", authDetails = authDetails)
    }

    suspend fun activities(
        authDetails: AuthDetails,
        page: Int,
        pageSize: Int,
    ): List<Activity> {
        return request(path = "athlete/activities", authDetails = authDetails) {
            append("per_page", pageSize.toString())
            append("page", page.toString())
        }
    }

    suspend fun activities(
        authDetails: AuthDetails,
        start: Long,
        end: Long,
        page: Int,
        pageSize: Int
    ): List<Activity> {
        return request(path = "athlete/activities", authDetails = authDetails) {
            append("after", start.milliseconds.inWholeSeconds.toString())
            append("before", end.milliseconds.inWholeSeconds.toString())
            append("per_page", pageSize.toString())
            append("page", page.toString())
        }
    }

    suspend fun routes(authDetails: AuthDetails, count: Int, page: Int): List<Route> {
        return request(path = "athlete/routes", authDetails = authDetails) {
            append("per_page", count.toString())
            append("page", page.toString())
        }
    }

    suspend fun routeGpx(authDetails: AuthDetails, id: Long): ByteArray {
        return request(path = "routes/$id/export_gpx", authDetails = authDetails)
    }

    @Throws(Throwable::class)
    private suspend inline fun <reified T> request(
        path: String,
        authDetails: AuthDetails,
        crossinline parameters: ParametersBuilder.() -> Unit = {},
    ): T {
        try {
            val accessToken = stravaAuthApiClient.accessToken(authDetails)
            return request(path = path, accessToken = accessToken, request = parameters)
        } catch (error: Throwable) {
            when (error) {
                is StravaError.AccessTokenInvalid -> {
                    val refreshedAccessToken =
                        stravaAuthApiClient.refreshAccessToken(authDetails.refreshToken)
                    return request(
                        path = path,
                        accessToken = refreshedAccessToken,
                        request = parameters
                    )
                }

                else -> {
                    throw error
                }
            }
        }
    }

    @Throws(Throwable::class)
    private suspend inline fun <reified T> request(
        path: String,
        accessToken: String,
        crossinline request: ParametersBuilder.() -> Unit,
    ): T {
        return withContext(Dispatchers.IO) {
            val call = client.get(path) {
                url {
                    headers.append("Authorization", "Bearer $accessToken")
                    parameters.request()
                }
            }.call
            try {
                call.request<T>()
            } catch (error: Throwable) {
                errorSubject.emit(error)
                throw error
            }
        }
    }
}
