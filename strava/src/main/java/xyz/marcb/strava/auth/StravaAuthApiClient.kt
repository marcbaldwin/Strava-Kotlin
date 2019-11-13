package xyz.marcb.strava.auth

import android.net.Uri
import io.reactivex.Single
import xyz.marcb.strava.AuthDetails
import xyz.marcb.strava.error.StravaError
import xyz.marcb.strava.error.StravaErrorAdapter

class StravaAuthApiClient(
        private val stravaAuthApi: StravaAuthApi,
        private val clientId: String,
        private val clientSecret: String
) {

    var onAuthDetailsRefreshed: ((AuthDetails) -> Unit)? = null

    fun authorizeUri(redirectUrl: String, scope: String): Uri {
        return Uri.parse("https://www.strava.com/oauth/mobile/authorize")
                .buildUpon()
                .appendQueryParameter("client_id", clientId)
                .appendQueryParameter("redirect_uri", redirectUrl)
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("approval_prompt", "force")
                .appendQueryParameter("scope", scope)
                .build()
    }

    fun authorize(uri: Uri): Single<StravaAuthResponse> {
        val code = uri.getQueryParameter("code")

        if (code != null) {
            return stravaAuthApi.authorize(clientId, clientSecret, code)
                    .onErrorResumeNext { error: Throwable ->
                        Single.error(StravaErrorAdapter.convert(error))
                    }
        }

        val error = when (val value = uri.getQueryParameter("error")) {
            "access_denied" -> StravaError.AuthUserDeniedAccess
            else -> StravaError.AuthUnexpectedError(value)
        }

        return Single.error(error)
    }

    fun scopes(uri: Uri): List<String>? {
        return uri.getQueryParameter("scope")?.split(",")
    }

    fun accessToken(authDetails: AuthDetails): Single<String> {
        return when (authDetails.hasExpired) {
            true -> refreshAccessToken(authDetails.refreshToken)
            false -> Single.just(authDetails.accessToken)
        }
    }

    fun refreshAccessToken(refreshToken: String): Single<String> {
        return stravaAuthApi.refreshToken(clientId, clientSecret, refreshToken)
                .doOnSuccess { response -> onAuthDetailsRefreshed?.invoke(response.authDetails) }
                .map { response -> response.access_token }
                .onErrorResumeNext { error: Throwable ->
                    Single.error(StravaErrorAdapter.convert(error))
                }
    }
}
