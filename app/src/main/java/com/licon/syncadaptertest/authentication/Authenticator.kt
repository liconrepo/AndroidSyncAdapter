package com.licon.syncadaptertest.authentication

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log

/** Account authenticator of the account type. */
class Authenticator(private val context: Context) : AbstractAccountAuthenticator(context) {

    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String?,
        requiredFeatures: Array<String>?,
        options: Bundle?,
    ): Bundle {
        Log.d(TAG, "> addAccount")

        val intent = signInIntent(response, accountType, authTokenType)
            .putExtra(SignInAuthenticatorActivity.ARG_IS_ADDING_NEW_ACCOUNT, true)
        return Bundle().apply { putParcelable(AccountManager.KEY_INTENT, intent) }
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle?,
    ): Bundle {
        Log.d(TAG, "> getAuthToken")

        if (authTokenType != AccountGeneral.AUTHTOKEN_TYPE_READ_ONLY &&
            authTokenType != AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS
        ) {
            return Bundle().apply { putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType") }
        }

        val accountManager = AccountManager.get(context)
        var authToken = accountManager.peekAuthToken(account, authTokenType)
        Log.d(TAG, "> peekAuthToken returned - $authToken")

        // Give another try to authenticate the user with the stored password
        if (TextUtils.isEmpty(authToken)) {
            accountManager.getPassword(account)?.let { password ->
                Log.d(TAG, "> re-authenticating with the existing password")
                authToken = runCatching {
                    AccountGeneral.serverAuthenticate.userSignIn(account.name, password, authTokenType)
                }.getOrNull()
            }
        }

        if (!TextUtils.isEmpty(authToken)) {
            return Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                putString(AccountManager.KEY_AUTHTOKEN, authToken)
            }
        }

        // The password is not available, so the user has to enter the credentials again
        val intent = signInIntent(response, account.type, authTokenType)
            .putExtra(SignInAuthenticatorActivity.ARG_ACCOUNT_NAME, account.name)
        return Bundle().apply { putParcelable(AccountManager.KEY_INTENT, intent) }
    }

    override fun getAuthTokenLabel(authTokenType: String?): String = when (authTokenType) {
        AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS -> AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS_LABEL
        AccountGeneral.AUTHTOKEN_TYPE_READ_ONLY -> AccountGeneral.AUTHTOKEN_TYPE_READ_ONLY_LABEL
        else -> "$authTokenType (Label)"
    }

    override fun hasFeatures(
        response: AccountAuthenticatorResponse,
        account: Account,
        features: Array<String>,
    ): Bundle = Bundle().apply { putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false) }

    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle? = null

    override fun confirmCredentials(
        response: AccountAuthenticatorResponse,
        account: Account,
        options: Bundle?,
    ): Bundle? = null

    override fun updateCredentials(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String?,
        options: Bundle?,
    ): Bundle? = null

    private fun signInIntent(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String?,
    ): Intent = Intent(context, SignInAuthenticatorActivity::class.java)
        .putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        .putExtra(SignInAuthenticatorActivity.ARG_ACCOUNT_TYPE, accountType)
        .putExtra(SignInAuthenticatorActivity.ARG_AUTH_TYPE, authTokenType)

    private companion object {
        const val TAG = "Authenticator"
    }
}
