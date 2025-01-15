package xyz.marcb.strava.auth

object StravaAuthScopes {

    // Read public segments, public routes, public profile data, public posts, public events, club feeds, and leaderboards
    const val read = "read"

    // Read private routes, private segments, and private events for the user
    const val readAll = "read_all"

    // Read all profile information even if the user has set their profile visibility to Followers or Only You
    const val profileReadAll = "profile:read_all"

    // Update the user's weight and Functional Threshold Power (FTP), and access to star or unstar segments on their behalf
    const val profileWrite = "profile:write"

    // Read the user's activity data for activities that are visible to Everyone and Followers, excluding privacy zone data
    const val activityRead = "activity:read"

    // The same access as activity:read, plus privacy zone data and access to read the user's activities with visibility set to Only You
    const val activityReadAll = "activity:read_all"

    // Access to create manual activities and uploads, and access to edit any activities that are visible to the app, based on activity read access level
    const val activityWrite = "activity:write"
}
