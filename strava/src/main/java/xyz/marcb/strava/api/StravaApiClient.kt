package xyz.marcb.strava.api

import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import xyz.marcb.strava.Activity
import xyz.marcb.strava.ActivityUploadStatus
import xyz.marcb.strava.AuthDetails
import xyz.marcb.strava.Route
import xyz.marcb.strava.auth.StravaAuthApiClient
import xyz.marcb.strava.error.StravaError
import xyz.marcb.strava.error.StravaErrorAdapter
import java.io.File
import java.lang.Error
import java.util.concurrent.TimeUnit

class StravaApiClient(
    private val stravaAuthApiClient: StravaAuthApiClient,
    private val stravaApi: StravaApi
) {

    private val errorSubject = PublishSubject.create<Throwable>()

    val errors: Observable<Throwable> = errorSubject.hide()

    fun activities(authDetails: AuthDetails, page: Int, pageSize: Int): Single<List<Activity>> {
        return request(authDetails) { accessToken ->
            stravaApi.activities(accessToken = accessToken, count = pageSize, page = page)
        }
    }

    fun activities(
        authDetails: AuthDetails,
        start: Long,
        end: Long,
        page: Int,
        pageSize: Int
    ): Single<List<Activity>> {
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

    fun routes(authDetails: AuthDetails, count: Int, page: Int): Single<List<Route>> {
        return request(authDetails) { accessToken ->
            stravaApi.routes(
                accessToken = accessToken,
                count = count,
                page = page
            )
        }
    }

    fun upload(
        authDetails: AuthDetails,
        externalId: String,
        dataType: String,
        activityType: String,
        file: File
    ): Single<ActivityUploadStatus> {
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

    fun uploadStatus(authDetails: AuthDetails, uploadId: Long): Single<ActivityUploadStatus> {
        return request(authDetails) { accessToken ->
            stravaApi.uploadStatus(accessToken = accessToken, uploadId = uploadId)
        }
    }

    private fun <T> request(authDetails: AuthDetails, request: (String) -> Single<T>): Single<T> {
        return stravaAuthApiClient.accessToken(authDetails)
            .flatMap { accessToken -> request(accessToken, request) }
            .onErrorResumeNext { error: Throwable ->
                when (StravaErrorAdapter.convert(error)) {
                    is StravaError.AccessTokenInvalid -> {
                        stravaAuthApiClient.refreshAccessToken(authDetails.refreshToken)
                            .flatMap { accessToken -> request(accessToken, request) }
                    }
                    else -> Single.error(error)
                }
            }
            .doOnError(errorSubject::onNext)
    }

    private fun <T> request(accessToken: String, request: (String) -> Single<T>): Single<T> {
        return request("Bearer $accessToken")
            .onErrorResumeNext { error: Throwable ->
                Single.error(StravaErrorAdapter.convert(error))
            }
    }
}