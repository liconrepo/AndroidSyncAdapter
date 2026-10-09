package com.licon.syncadaptertest.authentication

/** Constants shared by the authenticator, the sync adapter and the UI. */
object AccountGeneral {

    /** Account type id. */
    const val ACCOUNT_TYPE = "com.licon.syncadaptertest"

    /** Account user data field holding the Parse.com object id. */
    const val USERDATA_USER_OBJ_ID = "userObjectId"

    const val AUTHTOKEN_TYPE_READ_ONLY = "Read only"
    const val AUTHTOKEN_TYPE_READ_ONLY_LABEL = "Read only access to an licon-sync account"

    const val AUTHTOKEN_TYPE_FULL_ACCESS = "Full access"
    const val AUTHTOKEN_TYPE_FULL_ACCESS_LABEL = "Full access to an licon-sync account"

    val serverAuthenticate: ServerAuthenticate = ParseComServer()
}
