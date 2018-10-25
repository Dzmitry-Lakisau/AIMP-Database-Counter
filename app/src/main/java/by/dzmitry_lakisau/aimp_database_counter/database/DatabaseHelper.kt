package by.dzmitry_lakisau.aimp_database_counter.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper

internal class DatabaseHelper(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteAssetHelper(context, name, factory, version)
