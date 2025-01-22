package xyz.marcb.strava

import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.body
import xyz.marcb.strava.error.StravaErrorAdapter
import xyz.marcb.strava.error.StravaErrorResponse

@Suppress("MagicNumber")
internal suspend inline fun <reified T> HttpClientCall.request(): T {
    if (response.status.value in 200..299) {
        return body<T>()
    } else {
        val errorResponse = body<StravaErrorResponse>()
        val error = StravaErrorAdapter.convert(errorResponse, response.status.value)
        throw error
    }
}
