package xyz.marcb.strava.uri

import android.net.Uri

class AndroidWrappedUri(private val uri: Uri): WrappedUri {
    override fun getQueryParameter(key: String) = uri.getQueryParameter(key)
}
