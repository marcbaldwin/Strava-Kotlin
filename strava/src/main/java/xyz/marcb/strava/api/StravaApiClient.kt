package xyz.marcb.strava.api

import io.reactivex.Observable
import xyz.marcb.strava.Activity
import xyz.marcb.strava.ActivityUploadStatus
import xyz.marcb.strava.AuthDetails
import xyz.marcb.strava.Route
import xyz.marcb.strava.auth.StravaAuthApiClient
import xyz.marcb.strava.error.StravaError
import xyz.marcb.strava.error.StravaErrorAdapter
import java.io.File
import java.util.concurrent.TimeUnit

class StravaApiClient(
        private val stravaAuthApiClient: StravaAuthApiClient,
        private val stravaApi: StravaApi
) {

    fun activities(authDetails: AuthDetails, page: Int, pageSize: Int): Observable<List<Activity>> {
        return request(authDetails) { accessToken ->
            stravaApi.activities(accessToken = accessToken, count = pageSize, page = page)
        }
    }

    fun activities(authDetails: AuthDetails, start: Long, end: Long, page: Int, pageSize: Int): Observable<List<Activity>> {
        return request(authDetails) { accessToken ->
            stravaApi.activities(
                accessToken = accessToken,
                after = TimeUnit.MILLISECONDS.toSeconds(start),
                before = TimeUnit.MILLISECONDS.toSeconds(end),
                count = pageSize,
                page = page
            )
        }
    }

    fun routes(authDetails: AuthDetails, count: Int, page: Int): Observable<List<Route>> {
        return request(authDetails) { accessToken ->
            stravaApi.routes(
                accessToken = accessToken,
                count = count,
                page = page
            )
        }
    }

    fun upload(authDetails: AuthDetails, externalId: String, dataType: String, activityType: String, file: File): Observable<ActivityUploadStatus> {
        return request(authDetails) { accessToken ->
            stravaApi.upload(
                accessToken = accessToken,
                externalId = externalId,
                dataType = dataType,
                activityType = activityType,
                file = file
            )
        }
    }

    fun uploadStatus(authDetails: AuthDetails, uploadId: Long): Observable<ActivityUploadStatus> {
        return request(authDetails) { accessToken ->
            stravaApi.uploadStatus(accessToken = accessToken, uploadId = uploadId)
        }
    }

    private fun <T> request(authDetails: AuthDetails, request: (String) -> Observable<T>): Observable<T> {
        return stravaAuthApiClient.accessToken(authDetails)
                .switchMap { accessToken -> request(accessToken, request) }
                .onErrorResumeNext { error: Throwable ->
                    when (StravaErrorAdapter.convert(error)) {
                        is StravaError.AccessTokenInvalid -> {
                            stravaAuthApiClient.refreshAccessToken(authDetails.refreshToken)
                                    .switchMap { accessToken -> request(accessToken, request) }
                        }
                        else -> Observable.error(error)
                    }
                }
    }

    private fun <T> request(accessToken: String, request: (String) -> Observable<T>): Observable<T> {
        return request("Bearer $accessToken")
                .onErrorResumeNext { error: Throwable ->
                    Observable.error(StravaErrorAdapter.convert(error))
                }
    }
}