package com.licon.syncadaptertest.db

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils

/** Content provider exposing the local TV shows. */
class TvShowsContentProvider : ContentProvider() {

    private lateinit var dbHelper: TvShowsDbHelper

    override fun onCreate(): Boolean {
        dbHelper = TvShowsDbHelper(context!!)
        return true
    }

    override fun getType(uri: Uri): String = when (URI_MATCHER.match(uri)) {
        PATH_TOKEN -> TvShowsContract.CONTENT_TYPE_DIR
        PATH_FOR_ID_TOKEN -> TvShowsContract.CONTENT_ITEM_TYPE
        else -> throw UnsupportedOperationException("URI $uri is not supported.")
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?,
    ): Cursor? {
        val builder = SQLiteQueryBuilder().apply { tables = TvShowsDbHelper.TABLE_NAME }
        when (URI_MATCHER.match(uri)) {
            PATH_TOKEN -> Unit
            PATH_FOR_ID_TOKEN -> builder.appendWhere("${TvShowsDbHelper.COL_ID}=${ContentUris.parseId(uri)}")
            else -> return null
        }
        return builder.query(dbHelper.readableDatabase, projection, selection, selectionArgs, null, null, sortOrder)
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        if (URI_MATCHER.match(uri) != PATH_TOKEN) {
            throw UnsupportedOperationException("URI: $uri not supported.")
        }
        val id = dbHelper.writableDatabase.insert(TvShowsDbHelper.TABLE_NAME, null, values)
        if (id != -1L) {
            context!!.contentResolver.notifyChange(uri, null)
        }
        return ContentUris.withAppendedId(TvShowsContract.CONTENT_URI, id)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        val db = dbHelper.writableDatabase
        val rowsDeleted = when (URI_MATCHER.match(uri)) {
            PATH_TOKEN -> db.delete(TvShowsDbHelper.TABLE_NAME, selection, selectionArgs)
            PATH_FOR_ID_TOKEN -> {
                var where = "${TvShowsDbHelper.COL_ID}=${uri.lastPathSegment}"
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND $selection"
                }
                db.delete(TvShowsDbHelper.TABLE_NAME, where, selectionArgs)
            }
            else -> throw IllegalArgumentException("Unsupported URI: $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return rowsDeleted
    }

    /** Updates are not supported. */
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int = 0

    private companion object {
        const val PATH = "tvshows"
        const val PATH_TOKEN = 100
        const val PATH_FOR_ID = "tvshows/*"
        const val PATH_FOR_ID_TOKEN = 200

        val URI_MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(TvShowsContract.AUTHORITY, PATH, PATH_TOKEN)
            addURI(TvShowsContract.AUTHORITY, PATH_FOR_ID, PATH_FOR_ID_TOKEN)
        }
    }
}
