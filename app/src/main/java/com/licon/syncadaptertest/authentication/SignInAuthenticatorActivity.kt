package com.licon.syncadaptertest.authentication

import android.accounts.Account
import android.accounts.AccountAuthenticatorActivity
import android.accounts.AccountManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.licon.syncadaptertest.R

/**
 * The Authenticator activity.
 *
 * Called by the Authenticator and in charge of identifying the user.
 * It sends the result back to the Authenticator.
 */
class SignInAuthenticatorActivity : AccountAuthenticatorActivity() {

    private lateinit var accountManager: AccountManager
    private lateinit var authTokenType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        accountManager = AccountManager.get(baseContext)

        authTokenType = intent.getStringExtra(ARG_AUTH_TYPE) ?: AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS
        intent.getStringExtra(ARG_ACCOUNT_NAME)?.let { findViewById<TextView>(R.id.accountName).text = it }

        findViewById<TextView>(R.id.submit).setOnClickListener { submit() }
        findViewById<TextView>(R.id.signUp).setOnClickListener {
            // Since there can only be one SignInAuthenticatorActivity, the sign up activity result
            // is handed back through setAccountAuthenticatorResult(). See finishLogin().
            val signUp = Intent(baseContext, SignUpActivity::class.java)
            intent.extras?.let { signUp.putExtras(it) }
            @Suppress("DEPRECATION")
            startActivityForResult(signUp, REQ_SIGNUP)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK && data != null) {
            finishLogin(data)
        } else {
            @Suppress("DEPRECATION")
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun submit() {
        val userName = findViewById<TextView>(R.id.accountName).text.toString()
        val userPass = findViewById<TextView>(R.id.accountPassword).text.toString()

        Log.d(TAG, "> Started authenticating")
        authenticateAsync(
            accountName = userName,
            accountType = intent.getStringExtra(ARG_ACCOUNT_TYPE),
            password = userPass,
            authenticate = { AccountGeneral.serverAuthenticate.userSignIn(userName, userPass, authTokenType) },
            onSuccess = ::finishLogin,
        )
    }

    private fun finishLogin(result: Intent) {
        Log.d(TAG, "> finishLogin")

        val accountPassword = result.getStringExtra(PARAM_USER_PASS)
        val account = Account(
            result.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)!!,
            result.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)!!,
        )

        if (intent.getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d(TAG, "> finishLogin > addAccountExplicitly")
            // Setting the auth token avoids another call to the server to authenticate the user
            accountManager.addAccountExplicitly(account, accountPassword, null)
            accountManager.setAuthToken(account, authTokenType, result.getStringExtra(AccountManager.KEY_AUTHTOKEN))
        } else {
            Log.d(TAG, "> finishLogin > setPassword")
            accountManager.setPassword(account, accountPassword)
        }

        setAccountAuthenticatorResult(result.extras)
        setResult(RESULT_OK, result)
        finish()
    }

    companion object {
        const val ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE"
        const val ARG_AUTH_TYPE = "AUTH_TYPE"
        const val ARG_ACCOUNT_NAME = "ACCOUNT_NAME"
        const val ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT"

        private const val REQ_SIGNUP = 1
        private const val TAG = "SignInActivity"
    }
}
