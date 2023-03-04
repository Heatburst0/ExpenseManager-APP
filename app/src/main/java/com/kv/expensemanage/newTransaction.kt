package com.kv.expensemanage

import android.content.ContentValues
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_new_transaction.*
import kotlinx.coroutines.launch

class newTransaction : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_transaction)
        addTransac.setOnClickListener {
            adddata("ab",51,9,"nill")
//            MainActivity().update()
            finish()
        }
    }
    fun adddata( name : String,id : Int,price : Int,date  : String) {
        val helper = TransactionDatabase(applicationContext)
        val db = helper.readableDatabase
        lifecycleScope.launch {
            val cv = ContentValues()
            cv.put("id", id)
            cv.put("name", name)
            cv.put("price", price)
            cv.put("date", date)
            db.insert("record", null, cv)
        }
    }
}