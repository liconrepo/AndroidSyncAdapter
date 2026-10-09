package com.licon.syncadaptertest.syncadapter

import android.app.Service
import android.content.Intent
import android.os.IBinder

/** Service handing the [TvShowsSyncAdapter] binder to the sync framework. */
class TvShowsSyncService : Service() {

    override fun onCreate() {
        synchronized(lock) {
            if (syncAdapter == null) {
                syncAdapter = TvShowsSyncAdapter(applicationContext, true)
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? = syncAdapter?.syncAdapterBinder

    private companion object {
        val lock = Any()
        var syncAdapter: TvShowsSyncAdapter? = null
    }
}
