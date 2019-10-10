package xyz.marcb.strava.auth

import io.reactivex.Observable
import retrofit2.http.POST
import retrofit2.http.Query

interface StravaAuthApi {

    @POST("oauth/token?grant_type=authorization_code")
    fun authorize(
            @Query("client_id") clientId: String,
            @Query("client_secret") clientSecret: String,
            @Query("code") code: String
    ): Observable<StravaAuthResponse>

    @POST("oauth/token?grant_type=refresh_token")
    fun refreshToken(
            @Query("client_id") clientId: String,
            @Query("client_secret") clientSecret: String,
            @Query("refresh_token") refreshToken: String
    ): Observable<StravaAuthRefreshTokenResponse>
}
