package com.licon.syncadaptertest.authentication

import java.util.UUID

/**
 * Offline [ServerAuthenticate] used since Parse.com was discontinued.
 * It keeps no users: it validates the input and hands out a random token.
 * To use a real backend, implement [ServerAuthenticate] and set it in [AccountGeneral.serverAuthenticate].
 */
class DemoServerAuthenticate : ServerAuthenticate {

    override fun userSignUp(name: String, email: String, pass: String, authType: String): String {
        require(name.isNotEmpty()) { "Please enter your name" }
        requireValidCredentials(email, pass)
        return newToken()
    }

    override fun userSignIn(user: String, pass: String, authType: String): String {
        requireValidCredentials(user, pass)
        return newToken()
    }

    private fun requireValidCredentials(email: String, pass: String) {
        require(EMAIL_REGEX.matches(email)) { "Please enter a valid email address" }
        require(pass.length >= MIN_PASSWORD_LENGTH) { "Password must be at least $MIN_PASSWORD_LENGTH characters" }
    }

    private fun newToken(): String = UUID.randomUUID().toString()

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
