package com.licon.syncadaptertest.syncadapter

import android.util.Log
import com.google.gson.Gson
import com.licon.syncadaptertest.LOG_TAG
import com.licon.syncadaptertest.authentication.ParseComServer
import com.licon.syncadaptertest.authentication.ParseComServer.toException
import com.licon.syncadaptertest.db.dao.TvShow
import org.json.JSONObject
import java.net.HttpURLConnection

/** Reads and writes the TV shows stored on Parse.com. */
class ParseComServerAccessor {

    private class TvShows(val results: List<TvShow> = emptyList())

    /** Returns the TV shows visible to the user owning [auth]. */
    @Throws(Exception::class)
    fun getShows(auth: String): List<TvShow> {
        Log.d(LOG_TAG, "getShows auth[$auth]")

        val response = ParseComServer.request("GET", SHOWS_URL, sessionToken = auth)
        Log.d(LOG_TAG, "getShows> Response= ${response.body}")
        if (response.code != HttpURLConnection.HTTP_OK) {
            throw response.toException("Error retrieving tv shows")
        }
        return Gson().fromJson(response.body, TvShows::class.java).results
    }

    /** Stores [show] on the server, readable and writable only by [userId] when given. */
    @Throws(Exception::class)
    fun putShow(authToken: String, userId: String?, show: TvShow) {
        Log.d(LOG_TAG, "putShow [${show.name}]")

        val acl = JSONObject().put("*", JSONObject())
        userId?.let { acl.put(it, JSONObject().put("read", true).put("write", true)) }
        val request = JSONObject()
            .put("name", show.name)
            .put("year", show.year)
            .put("ACL", acl)
            .toString()
        Log.d(LOG_TAG, "Request = $request")

        val response = ParseComServer.request("POST", SHOWS_URL, request, authToken)
        if (response.code != HttpURLConnection.HTTP_CREATED) {
            throw response.toException("Error posting tv shows")
        }
    }

    private companion object {
        const val SHOWS_URL = "https://api.parse.com/1/classes/tvshows"
    }
}
