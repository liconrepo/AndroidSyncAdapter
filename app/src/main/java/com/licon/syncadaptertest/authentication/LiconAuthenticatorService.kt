package com.licon.syncadaptertest.authentication

import android.app.Service
import android.content.Intent
import android.os.IBinder

/** Exposes [LiconAuthenticator] to the account manager. */
class LiconAuthenticatorService : Service() {
    override fun onBind(intent: Intent): IBinder? = LiconAuthenticator(this).iBinder
}
