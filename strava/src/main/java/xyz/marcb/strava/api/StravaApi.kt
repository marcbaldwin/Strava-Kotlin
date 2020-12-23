package xyz.marcb.strava.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import xyz.marcb.strava.Activity
import xyz.marcb.strava.ActivityUploadStatus
import xyz.marcb.strava.Athlete
import xyz.marcb.strava.Route
import java.io.File

interface StravaApi {

    @GET("athlete")
    fun athlete(
        @Header("Authorization") accessToken: String
    ): Single<Athlete>

    @GET("athlete/activities")
    fun activities(
        @Header("Authorization") accessToken: String,
        @Query("per_page") count: Int,
        @Query("page") page: Int
    ): Single<List<Activity>>

    @GET("athlete/activities")
    fun activities(
        @Header("Authorization") accessToken: String,
        @Query("after") after: Long,
        @Query("before") before: Long,
        @Query("per_page") count: Int,
        @Query("page") page: Int
    ): Single<List<Activity>>

    @GET("athlete/routes")
    fun routes(
        @Header("Authorization") accessToken: String,
        @Query("per_page") count: Int,
        @Query("page") page: Int
    ): Single<List<Route>>

    @Multipart
    @POST("uploads")
    fun upload(
        @Header("Authorization") accessToken: String,
        @Part("external_id") externalId: String,
        @Part("data_type") dataType: String,
        @Part("activity_type") activityType: String? = null,
        @Part("file\"; filename=\"file") file: File // horrible injection hack to set "filename", which is required by Strava
    ): Single<ActivityUploadStatus>

    @GET("uploads/{id}")
    fun uploadStatus(
        @Header("Authorization") accessToken: String,
        @Path("id") uploadId: Long
    ): Single<ActivityUploadStatus>
}
