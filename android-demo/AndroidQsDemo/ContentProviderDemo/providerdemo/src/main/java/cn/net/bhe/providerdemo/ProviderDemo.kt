package cn.net.bhe.providerdemo

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.net.Uri

class ProviderDemo : ContentProvider() {

    private val dbName = "content_provider_demo.db"
    private val dbVersion = 1
    private var dbHelper: DbHelper? = null
    private var database: SQLiteDatabase? = null

    private val uriAuthority = "cn.net.bhe.providerdemo"
    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private val tableLog = "t_log"
    private val tableLogCode = 1
    private val tableLogIdCode = 11

    init {
        uriMatcher.addURI(uriAuthority, tableLog, tableLogCode)
        uriMatcher.addURI(uriAuthority, "$tableLog/#", tableLogIdCode)
    }

    override fun onCreate(): Boolean {
        dbHelper = DbHelper(context!!, dbName, null, dbVersion)
        database = dbHelper?.writableDatabase
        return database != null
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        return when (uriMatcher.match(uri)) {
            tableLogCode -> database?.query(tableLog, null, null, null, null, null, null)
            tableLogIdCode -> database?.query(tableLog, null, " id = ${uri.lastPathSegment} ", null, null, null, null)

            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
    }

    override fun getType(uri: Uri): String? {
        // TODO
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val table = when (uriMatcher.match(uri)) {
            tableLogCode -> tableLog
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        val insertRet = database?.insert(table, null, values)
        if (insertRet != null && insertRet > 0) {
            return Uri.parse("content://$uriAuthority/$table/$insertRet")
        } else {
            throw SQLException("Insertion failed for $uri")
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        // TODO
        return 0
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        // TODO
        return 0
    }

    class DbHelper(
        context: Context,
        name: String,
        factory: SQLiteDatabase.CursorFactory?,
        version: Int
    ) : SQLiteOpenHelper(context, name, factory, version) {

        private val createLog = " create table if not exists t_log(id text, ip text, op text, val text) "
        private val dropLog = " drop table if exists t_log "

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(createLog)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL(dropLog)
        }

    }

}