package xyz.marcb.strava.uri

interface UriBuilder {
    fun setPath(path: String): UriBuilder
    fun appendQueryParameter(key: String, value: String): UriBuilder
}
