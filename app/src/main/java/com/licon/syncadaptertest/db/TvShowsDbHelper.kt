package com.licon.syncadaptertest.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/** Creates and upgrades the TV shows database. */
class TvShowsDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "create table $TABLE_NAME(" +
                "$COL_ID integer primary key autoincrement, " +
                "$COL_NAME text not null, " +
                "$COL_YEAR integer);",
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.w(TAG, "Upgrading database from version $oldVersion to $newVersion, which will destroy all old data")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    companion object {
        const val TABLE_NAME = "tvshows"
        const val COL_ID = "_id"
        const val COL_NAME = "name"
        const val COL_YEAR = "year"

        private const val TAG = "TvShowsDbHelper"
        private const val DATABASE_NAME = "licon_sync.db"
        private const val DATABASE_VERSION = 1
    }
}
