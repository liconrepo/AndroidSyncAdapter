package com.licon.syncadaptertest.syncadapter

import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AuthenticatorException
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.util.Log
import com.licon.syncadaptertest.LOG_TAG
import com.licon.syncadaptertest.authentication.AccountGeneral
import com.licon.syncadaptertest.db.TvShowsContract
import com.licon.syncadaptertest.db.dao.TvShow
import java.io.IOException

/** Two-way sync of the local TV shows with the ones stored on Parse.com. */
class TvShowsSyncAdapter(context: Context, autoInitialize: Boolean) :
    AbstractThreadedSyncAdapter(context, autoInitialize) {

    private val accountManager = AccountManager.get(context)

    override fun onPerformSync(
        account: Account,
        extras: Bundle?,
        authority: String,
        provider: ContentProviderClient,
        syncResult: SyncResult,
    ) {
        val extrasLog = extras?.keySet()?.joinToString(" ") { "$it[${extras.get(it)}]" }
        Log.d(LOG_TAG, "$TAG> onPerformSync for account[${account.name}]. Extras: $extrasLog")

        try {
            // The auth token and the userObjectId are needed for creating items on Parse.com
            val authToken = accountManager.blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, true)
                ?: throw AuthenticatorException("No auth token for ${account.name}")
            val userObjectId = accountManager.getUserData(account, AccountGeneral.USERDATA_USER_OBJ_ID)
            val parseComService = ParseComServerAccessor()

            Log.d(LOG_TAG, "$TAG> Get remote TV Shows")
            val remoteTvShows = parseComService.getShows(authToken)

            Log.d(LOG_TAG, "$TAG> Get local TV Shows")
            val localTvShows = buildList {
                provider.query(TvShowsContract.CONTENT_URI, null, null, null, null)?.use { cursor ->
                    while (cursor.moveToNext()) {
                        add(TvShow.fromCursor(cursor))
                    }
                }
            }

            val showsToRemote = localTvShows.filter { it !in remoteTvShows }
            val showsToLocal = remoteTvShows.filter { it !in localTvShows && it.year != 1 } // TODO REMOVE THIS

            if (showsToRemote.isEmpty()) {
                Log.d(LOG_TAG, "$TAG> No local changes to update server")
            } else {
                Log.d(LOG_TAG, "$TAG> Updating remote server with local changes")
                for (show in showsToRemote) {
                    Log.d(LOG_TAG, "$TAG> Local -> Remote [${show.name}]")
                    parseComService.putShow(authToken, userObjectId, show)
                }
            }

            if (showsToLocal.isEmpty()) {
                Log.d(LOG_TAG, "$TAG> No server changes to update local database")
            } else {
                Log.d(LOG_TAG, "$TAG> Updating local database with remote changes")
                showsToLocal.forEach { Log.d(LOG_TAG, "$TAG> Remote -> Local [${it.name}]") }
                provider.bulkInsert(
                    TvShowsContract.CONTENT_URI,
                    showsToLocal.map { it.toContentValues() }.toTypedArray(),
                )
            }

            Log.d(LOG_TAG, "$TAG> Finished.")
        } catch (e: IOException) {
            syncResult.stats.numIoExceptions++
            Log.e(LOG_TAG, "$TAG> Sync failed", e)
        } catch (e: AuthenticatorException) {
            syncResult.stats.numAuthExceptions++
            Log.e(LOG_TAG, "$TAG> Sync failed", e)
        } catch (e: Exception) {
            Log.e(LOG_TAG, "$TAG> Sync failed", e)
        }
    }

    private companion object {
        const val TAG = "TvShowsSyncAdapter"
    }
}
