package com.kv.expensemanage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TransactionDatabase(context : Context) : SQLiteOpenHelper(context,"Userdb",null,1){
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("create table record(id number primary key,name text,price number(8,4),date text)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }
}