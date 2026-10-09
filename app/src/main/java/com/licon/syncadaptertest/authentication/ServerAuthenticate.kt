package com.licon.syncadaptertest.authentication

/** Server side of the account authentication. */
interface ServerAuthenticate {

    /** Creates a new user and returns its session token. */
    @Throws(Exception::class)
    fun userSignUp(name: String, email: String, pass: String, authType: String): String?

    /** Signs an existing user in and returns its session token. */
    @Throws(Exception::class)
    fun userSignIn(user: String, pass: String, authType: String): String?
}
