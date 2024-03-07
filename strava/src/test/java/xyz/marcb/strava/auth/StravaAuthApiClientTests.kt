package xyz.marcb.strava.auth

import android.net.Uri
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.whenever
import xyz.marcb.strava.Athlete
import xyz.marcb.strava.error.StravaError

class StravaAuthApiClientTests {

    @Rule
    @JvmField
    val rule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var stravaAuthApi: StravaAuthApi

    private lateinit var stravaAuthApiClient: StravaAuthApiClient

    @Before
    fun setup() {
        stravaAuthApiClient = StravaAuthApiClient(stravaAuthApi, "client_id", "client_secret")
    }

    @Test
    fun `authorize given valid response`() {
        val uri = Mockito.mock(Uri::class.java)
        whenever(uri.getQueryParameter("code")).thenReturn("1234")

        val response = StravaAuthResponse(
            "access_token",
            "refresh_token",
            1000L,
            Mockito.mock(Athlete::class.java)
        )
        whenever(stravaAuthApi.authorize("client_id", "client_secret", "1234"))
            .thenReturn(Single.just(response))

        stravaAuthApiClient.authorize(uri).test().assertValue(response)
    }

    @Test
    fun `authorize given valid response but token exchange errors`() {
        val uri = Mockito.mock(Uri::class.java)
        whenever(uri.getQueryParameter("code")).thenReturn("1234")

        val error = Error()
        whenever(stravaAuthApi.authorize("client_id", "client_secret", "1234"))
            .thenReturn(Single.error(error))

        stravaAuthApiClient.authorize(uri).test().assertError(error)
    }

    @Test
    fun `authorize given response is access denied`() {
        val uri = Mockito.mock(Uri::class.java)
        whenever(uri.getQueryParameter("error")).thenReturn("access_denied")
        stravaAuthApiClient.authorize(uri).test().assertError(StravaError.AuthUserDeniedAccess)
    }

    @Test
    fun `authorize given response is undefined`() {
        val uri = Mockito.mock(Uri::class.java)
        stravaAuthApiClient.authorize(uri).test()
            .assertError { it is StravaError.AuthUnexpectedError }
    }
}
