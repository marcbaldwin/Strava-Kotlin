package xyz.marcb.strava.uri

import android.net.Uri

class AndroidUriBuilder: UriBuilder {

    private var uriBuilder: Uri.Builder? = null

    override fun setPath(path: String): UriBuilder {
        uriBuilder = Uri.parse(path).buildUpon()
        return this
    }

    override fun appendQueryParameter(key: String, value: String): UriBuilder {
        uriBuilder?.appendQueryParameter(key, value)
        return this
    }

    fun build() = uriBuilder?.build()
}
