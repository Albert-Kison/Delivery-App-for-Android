package com.griffith.deliveryapp

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseManager(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int): SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createCommand)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(dropCommand)
        db?.execSQL(createCommand)
    }

    private val createCommand = "CREATE TABLE AppValues(Id INTEGER PRIMARY KEY AUTOINCREMENT, Name TEXT, Value TEXT)"
    private val dropCommand = "DROP TABLE AppValues"

}