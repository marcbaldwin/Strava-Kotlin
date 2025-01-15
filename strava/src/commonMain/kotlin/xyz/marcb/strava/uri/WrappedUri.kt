package xyz.marcb.strava.uri

interface WrappedUri {
    fun getQueryParameter(key: String): String?
}
