package com.licon.syncadaptertest

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.SyncStatusObserver
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import com.licon.syncadaptertest.authentication.AccountGeneral
import com.licon.syncadaptertest.db.TvShowsContract
import com.licon.syncadaptertest.db.dao.TvShow
import com.licon.syncadaptertest.syncadapter.ParseComServerAccessor
import kotlin.concurrent.thread
import kotlin.random.Random

/** Demo screen driving the account, the local TV shows and the sync adapter. */
class MainActivity : Activity() {

    private lateinit var accountManager: AccountManager
    private var authToken: String? = null
    private var connectedAccount: Account? = null
    private var syncObserverHandle: Any? = null

    private val syncObserver = SyncStatusObserver { runOnUiThread { refreshSyncStatus() } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        accountManager = AccountManager.get(this)

        findViewById<TextView>(R.id.btnShowRemoteList).setOnClickListener { showRemoteList() }
        findViewById<TextView>(R.id.btnAddShow).setOnClickListener { addRandomShow() }
        findViewById<TextView>(R.id.btnShowLocalList).setOnClickListener { showOnDialog(null, readLocalShows()) }
        findViewById<TextView>(R.id.btnClearLocalData).setOnClickListener {
            val numDeleted = contentResolver.delete(TvShowsContract.CONTENT_URI, null, null)
            toast("Deleted $numDeleted TV shows from the local list")
        }

        findViewById<TextView>(R.id.btnConnect).setOnClickListener { connect() }
        findViewById<TextView>(R.id.btnSync).setOnClickListener {
            val account = connectedAccount ?: return@setOnClickListener toast(CONNECT_FIRST)
            val extras = Bundle().apply {
                // Perform a sync no matter if it's off
                putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true)
                putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true)
            }
            ContentResolver.requestSync(account, TvShowsContract.AUTHORITY, extras)
        }

        findViewById<CheckBox>(R.id.cbIsSyncable).setOnCheckedChangeListener { _, isChecked ->
            val account = connectedAccount ?: return@setOnCheckedChangeListener toast(CONNECT_FIRST)
            ContentResolver.setIsSyncable(account, TvShowsContract.AUTHORITY, if (isChecked) 1 else 0)
        }
        findViewById<CheckBox>(R.id.cbAutoSync).setOnCheckedChangeListener { _, isChecked ->
            val account = connectedAccount ?: return@setOnCheckedChangeListener toast(CONNECT_FIRST)
            ContentResolver.setSyncAutomatically(account, TvShowsContract.AUTHORITY, isChecked)
        }
    }

    override fun onResume() {
        super.onResume()
        syncObserverHandle = ContentResolver.addStatusChangeListener(
            ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE or ContentResolver.SYNC_OBSERVER_TYPE_PENDING,
            syncObserver,
        )
    }

    override fun onPause() {
        syncObserverHandle?.let { ContentResolver.removeStatusChangeListener(it) }
        super.onPause()
    }

    @Suppress("DEPRECATION")
    private fun showRemoteList() {
        val token = authToken ?: return toast(CONNECT_FIRST)
        val progressDialog = ProgressDialog(this).apply { show() }
        thread {
            val shows = try {
                ParseComServerAccessor().getShows(token)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Failed to get the remote TV shows", e)
                null
            }
            runOnUiThread {
                progressDialog.dismiss()
                shows?.let { showOnDialog("Remote TV Shows", it) }
            }
        }
    }

    private fun addRandomShow() {
        val names = resources.getStringArray(R.array.tvshows_names)
        val years = resources.getIntArray(R.array.tvshows_year)
        val index = Random.nextInt(names.size)

        val tvShow = TvShow(names[index], years[index])
        Log.d(LOG_TAG, "Tv Show to add [id=$index]: $tvShow")

        contentResolver.insert(TvShowsContract.CONTENT_URI, tvShow.toContentValues())
        toast("Added \"$tvShow\"")
    }

    private fun showOnDialog(title: String?, tvShows: List<TvShow>) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, tvShows), null)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun refreshSyncStatus() {
        val account = connectedAccount ?: return
        val status = when {
            ContentResolver.isSyncActive(account, TvShowsContract.AUTHORITY) -> "Status: Syncing..."
            ContentResolver.isSyncPending(account, TvShowsContract.AUTHORITY) -> "Status: Pending..."
            else -> "Status: Idle"
        }
        findViewById<TextView>(R.id.status).text = status
        Log.d(LOG_TAG, "refreshSyncStatus> $status")
    }

    private fun initButtonsAfterConnect(account: Account) {
        // Init the checkboxes according to the sync adapter settings
        val authority = TvShowsContract.AUTHORITY
        findViewById<CheckBox>(R.id.cbIsSyncable).isChecked = ContentResolver.getIsSyncable(account, authority) > 0
        findViewById<CheckBox>(R.id.cbAutoSync).isChecked = ContentResolver.getSyncAutomatically(account, authority)

        for (id in intArrayOf(R.id.cbIsSyncable, R.id.cbAutoSync, R.id.status, R.id.btnShowRemoteList, R.id.btnSync)) {
            findViewById<TextView>(id).isEnabled = true
        }
        findViewById<TextView>(R.id.btnConnect).isEnabled = false

        refreshSyncStatus()
    }

    private fun readLocalShows(): List<TvShow> = buildList {
        contentResolver.query(TvShowsContract.CONTENT_URI, null, null, null, null)?.use { cursor ->
            while (cursor.moveToNext()) {
                add(TvShow.fromCursor(cursor))
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun connect() {
        accountManager.getAuthTokenByFeatures(
            AccountGeneral.ACCOUNT_TYPE,
            AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS,
            null,
            this,
            null,
            null,
            { future ->
                try {
                    val result = future.result
                    authToken = result.getString(AccountManager.KEY_AUTHTOKEN)
                    authToken?.let {
                        val account = Account(result.getString(AccountManager.KEY_ACCOUNT_NAME)!!, AccountGeneral.ACCOUNT_TYPE)
                        connectedAccount = account
                        runOnUiThread { initButtonsAfterConnect(account) }
                    }
                    toast(authToken?.let { "SUCCESS!\ntoken: $it" } ?: "FAIL")
                    Log.d(LOG_TAG, "GetTokenForAccount Bundle is $result")
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Failed to get the auth token", e)
                    e.message?.let(::toast)
                }
            },
            null,
        )
    }

    private fun toast(message: String) {
        runOnUiThread { Toast.makeText(this, message, Toast.LENGTH_SHORT).show() }
    }

    private companion object {
        const val CONNECT_FIRST = "Please connect first"
    }
}
