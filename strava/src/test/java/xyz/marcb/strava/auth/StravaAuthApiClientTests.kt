package xyz.marcb.strava.auth

class StravaAuthApiClientTests {

//    @Rule
//    @JvmField
//    val rule: MockitoRule = MockitoJUnit.rule()
//
//    private lateinit var stravaAuthApiClient: StravaAuthApiClient
//
//    @Before
//    fun setup() {
//        stravaAuthApiClient = StravaAuthApiClient("client_id", "client_secret")
//    }
//
//    @Test
//    fun `authorize given valid response`() {
//        val uri = Mockito.mock(Uri::class.java)
//        whenever(uri.getQueryParameter("code")).thenReturn("1234")
//
//        val response = StravaAuthResponse(
//            "access_token",
//            "refresh_token",
//            1000L,
//            Mockito.mock(Athlete::class.java)
//        )
//        whenever(stravaAuthApi.authorize("client_id", "client_secret", "1234"))
//            .thenReturn(Single.just(response))
//
//        stravaAuthApiClient.authorize(uri).test().assertValue(response)
//    }
//
//    @Test
//    fun `authorize given valid response but token exchange errors`() {
//        val uri = Mockito.mock(Uri::class.java)
//        whenever(uri.getQueryParameter("code")).thenReturn("1234")
//
//        val error = Error()
//        whenever(stravaAuthApi.authorize("client_id", "client_secret", "1234"))
//            .thenReturn(Single.error(error))
//
//        stravaAuthApiClient.authorize(uri).test().assertError(error)
//    }
//
//    @Test
//    fun `authorize given response is access denied`() {
//        val uri = Mockito.mock(Uri::class.java)
//        whenever(uri.getQueryParameter("error")).thenReturn("access_denied")
//        stravaAuthApiClient.authorize(uri).test().assertError(StravaError.AuthUserDeniedAccess)
//    }
//
//    @Test
//    fun `authorize given response is undefined`() {
//        val uri = Mockito.mock(Uri::class.java)
//        stravaAuthApiClient.authorize(uri).test()
//            .assertError { it is StravaError.AuthUnexpectedError }
//    }
}
