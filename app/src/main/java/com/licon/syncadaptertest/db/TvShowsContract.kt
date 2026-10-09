package com.licon.syncadaptertest.db

import android.net.Uri

/** Contract of the TV shows content provider. */
object TvShowsContract {
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.licon.tvshow"
    const val CONTENT_TYPE_DIR = "vnd.android.cursor.dir/vnd.licon.tvshow"

    const val AUTHORITY = "com.licon.sync.tvshows.provider"

    /** content://<authority>/<path to type> */
    val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/tvshows")
}
