package com.licon.syncadaptertest.authentication

import android.app.Service
import android.content.Intent
import android.os.IBinder

/** Exposes [Authenticator] to the account manager. */
class AuthenticatorService : Service() {
    override fun onBind(intent: Intent): IBinder? = Authenticator(this).iBinder
}
