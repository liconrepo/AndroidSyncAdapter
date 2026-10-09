package com.licon.syncadaptertest.authentication

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import kotlin.concurrent.thread

internal const val KEY_ERROR_MESSAGE = "ERR_MSG"
internal const val PARAM_USER_PASS = "USER_PASS"

/**
 * Runs a blocking [authenticate] call off the main thread, then delivers either an intent with the
 * account details to [onSuccess] or shows the error message.
 */
internal fun Activity.authenticateAsync(
    accountName: String,
    accountType: String?,
    password: String,
    authenticate: () -> String?,
    onSuccess: (Intent) -> Unit,
) {
    thread {
        val result = Intent()
        try {
            val authToken = authenticate()
            result.putExtra(AccountManager.KEY_ACCOUNT_NAME, accountName)
            result.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
            result.putExtra(AccountManager.KEY_AUTHTOKEN, authToken)
            result.putExtra(PARAM_USER_PASS, password)
        } catch (e: Exception) {
            result.putExtra(KEY_ERROR_MESSAGE, e.message)
        }
        runOnUiThread {
            if (result.hasExtra(KEY_ERROR_MESSAGE)) {
                Toast.makeText(this, result.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show()
            } else {
                onSuccess(result)
            }
        }
    }
}
