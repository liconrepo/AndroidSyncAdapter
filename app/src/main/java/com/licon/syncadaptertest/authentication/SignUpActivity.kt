package com.licon.syncadaptertest.authentication

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.licon.syncadaptertest.R

/** Creates a new account on the server and hands it back to [SignInAuthenticatorActivity]. */
class SignUpActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        findViewById<TextView>(R.id.alreadyMember).setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        findViewById<TextView>(R.id.submit).setOnClickListener { createAccount() }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        @Suppress("DEPRECATION")
        super.onBackPressed()
    }

    private fun createAccount() {
        val name = findViewById<TextView>(R.id.name).text.toString().trim()
        val accountName = findViewById<TextView>(R.id.accountName).text.toString().trim()
        val accountPassword = findViewById<TextView>(R.id.accountPassword).text.toString().trim()

        Log.d(TAG, "> Started authenticating")
        authenticateAsync(
            accountName = accountName,
            accountType = intent.getStringExtra(SignInAuthenticatorActivity.ARG_ACCOUNT_TYPE),
            password = accountPassword,
            authenticate = {
                AccountGeneral.serverAuthenticate.userSignUp(
                    name, accountName, accountPassword, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,
                )
            },
            onSuccess = {
                setResult(RESULT_OK, it)
                finish()
            },
        )
    }

    private companion object {
        const val TAG = "SignUpActivity"
    }
}
