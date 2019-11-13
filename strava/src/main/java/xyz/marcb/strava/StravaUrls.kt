package xyz.marcb.strava

import android.net.Uri

object StravaUrls {

    fun activity(activityId: String): Uri =
        Uri.parse("https://www.strava.com/activities/$activityId")
}
