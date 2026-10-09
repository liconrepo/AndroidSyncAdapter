package com.licon.syncadaptertest.authentication

import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL

/** Minimal HTTP client for the Parse.com REST API. */
object ParseComServer {

    private const val APP_ID = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
    private const val REST_API_KEY = "BBBBBBBBBBBBBBBBBBBBBBBB"

    /** Error body returned by Parse.com. */
    data class ParseComError(val code: Int = 0, val error: String? = null)

    /** Raw HTTP result. */
    data class Response(val code: Int, val body: String)

    /**
     * Performs a Parse.com request with the app credentials.
     *
     * @param method HTTP method
     * @param url full request url
     * @param body optional JSON body
     * @param sessionToken optional user session token
     */
    fun request(
        method: String,
        url: String,
        body: String? = null,
        sessionToken: String? = null,
    ): Response {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.setRequestProperty("X-Parse-Application-Id", APP_ID)
            connection.setRequestProperty("X-Parse-REST-API-Key", REST_API_KEY)
            sessionToken?.let { connection.setRequestProperty("X-Parse-Session-Token", it) }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { it.write(body.toByteArray()) }
            }
            val code = connection.responseCode
            val stream = if (code < HttpURLConnection.HTTP_BAD_REQUEST) connection.inputStream else connection.errorStream
            return Response(code, stream?.bufferedReader()?.use { it.readText() }.orEmpty())
        } finally {
            connection.disconnect()
        }
    }

    /** Builds an exception out of a failed response. */
    fun Response.toException(message: String): Exception {
        val error = runCatching { Gson().fromJson(body, ParseComError::class.java) }.getOrNull()
        return Exception("$message [${error?.code ?: code}] - ${error?.error ?: body}")
    }
}
