package xyz.marcb.strava.api

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.http.ParametersBuilder
import io.ktor.serialization.kotlinx.json.json
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import xyz.marcb.strava.Activity
import xyz.marcb.strava.Athlete
import xyz.marcb.strava.AuthDetails
import xyz.marcb.strava.Route
import xyz.marcb.strava.auth.StravaAuthApiClient
import xyz.marcb.strava.error.StravaError
import xyz.marcb.strava.error.StravaErrorAdapter
import xyz.marcb.strava.error.StravaErrorResponse

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
            append("after", TimeUnit.MILLISECONDS.toSeconds(start).toString())
            append("before", TimeUnit.MILLISECONDS.toSeconds(end).toString())
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

//    suspend fun routeGpx(authDetails: AuthDetails, id: Long): ByteArray {
//        return request("routes/$id/export_gpx", authDetails) { accessToken ->
//            stravaApi.routeGpx(accessToken = accessToken, id = id)
//        }
//        .map { response ->
//            response.body()?.bytes()
//                ?: throw response.errorBody()?.string()
//                    ?.let { StravaErrorAdapter.convert(it, response.code()) }
//                    ?: Error("Unexpected error")
//        }
    /// @Path("id") id: Long
//    parametersBuilder.append("id", page)
//    }

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
        val call = client.get(path) {
            url {
                headers.append("Authorization", "Bearer $accessToken")
                parameters.request()
            }
        }.call
        return request<T>(call)
    }


    @Suppress("MagicNumber")
    private suspend inline fun <reified T> request(call: HttpClientCall): T {
        if (call.response.status.value in 200..299) {
            return call.body<T>()
        } else {
            val errorResponse = call.body<StravaErrorResponse>()
            val error = StravaErrorAdapter.convert(errorResponse, call.response.status.value)
            errorSubject.tryEmit(error)
            throw error
        }
    }
}
