package com.licon.syncadaptertest.db.dao

import android.content.ContentValues
import android.database.Cursor
import com.licon.syncadaptertest.db.TvShowsDbHelper
import java.io.Serializable

/** A TV show, stored locally and on the server. */
data class TvShow(val name: String, val year: Int) : Serializable {

    fun toContentValues(): ContentValues = ContentValues().apply {
        put(TvShowsDbHelper.COL_NAME, name)
        put(TvShowsDbHelper.COL_YEAR, year)
    }

    override fun toString(): String = "$name ($year)"

    companion object {
        /** Reads the TV show at the current cursor position. */
        fun fromCursor(cursor: Cursor): TvShow = TvShow(
            cursor.getString(cursor.getColumnIndexOrThrow(TvShowsDbHelper.COL_NAME)),
            cursor.getInt(cursor.getColumnIndexOrThrow(TvShowsDbHelper.COL_YEAR)),
        )
    }
}
