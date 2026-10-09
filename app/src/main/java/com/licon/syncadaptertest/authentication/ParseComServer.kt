package com.licon.syncadaptertest.authentication

import android.util.Log
import com.google.gson.Gson
import com.licon.syncadaptertest.LOG_TAG
import com.licon.syncadaptertest.model.User
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** [ServerAuthenticate] backed by the Parse.com REST API. */
class ParseComServer : ServerAuthenticate {

    /** Error body returned by Parse.com. */
    data class ParseComError(val code: Int = 0, val error: String? = null)

    /** Raw HTTP result. */
    data class Response(val code: Int, val body: String)

    override fun userSignUp(name: String, email: String, pass: String, authType: String): String? {
        val user = JSONObject()
            .put("username", email)
            .put("password", pass)
            .put("phone", "415-392-0202")
        val response = request("POST", "$BASE_URL/users", user.toString())
        if (response.code != HttpURLConnection.HTTP_CREATED) {
            throw response.toException("Error creating user")
        }
        return Gson().fromJson(response.body, User::class.java).sessionToken
    }

    override fun userSignIn(user: String, pass: String, authType: String): String? {
        Log.d(LOG_TAG, "userSignIn")
        val query = "username=${URLEncoder.encode(user, "UTF-8")}&password=${URLEncoder.encode(pass, "UTF-8")}"
        val response = request("GET", "$BASE_URL/login?$query")
        if (response.code != HttpURLConnection.HTTP_OK) {
            throw response.toException("Error signing-in")
        }
        return Gson().fromJson(response.body, User::class.java).sessionToken
    }

    companion object {
        private const val BASE_URL = "https://api.parse.com/1"
        private const val APP_ID = "iRnc8I1X0du5q6HrJtZW0a5DlB0JcpOQbjA6chha"
        private const val REST_API_KEY = "tv1xCdYKTwI3p205KHCn1yWpbVj2OHldV9cPZuNZ"

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
}
