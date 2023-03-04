package com.kv.expensemanage


data class Transaction(
    val id : Int,
    val label: String,
    val price: Double,
    val date : String,
    val time : String,
    val desc : String
)
