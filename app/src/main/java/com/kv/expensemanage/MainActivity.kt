package com.kv.expensemanage

import android.app.*
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.kv.expensemanage.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_new_transaction.*
import kotlinx.android.synthetic.main.alert_dialog.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private var Id=0
    private var neg=-1.0

    private lateinit var dlttransac : Transaction
    private lateinit var oldtransac : ArrayList<Transaction>
    lateinit var transactions : ArrayList<Transaction>

    private var currDate=""
    private var currTime=""

    private var text : TextWatcher?=null

    lateinit var Transactionadapter : TransactionAdapter
    private lateinit var layoutmanager : LinearLayoutManager

    private lateinit var alarmManager: AlarmManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var calendar: Calendar

    private var binding : ActivityMainBinding?=null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        transactions= arrayListOf()

        val da = Calendar.getInstance()
        val day = da.get(Calendar.DAY_OF_MONTH)
        val month= da.get(Calendar.MONTH)+1
        val yr = da.get(Calendar.YEAR)
        val hr=da.get(Calendar.HOUR_OF_DAY)
        val min=da.get(Calendar.MINUTE)

        currTime = "$hr : $min"
        currDate="$day/$month/$yr"

        binding?.date?.text=currDate
        createnotification()


        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                dltTransaction(transactions[viewHolder.adapterPosition])
            }

        }

        val swipe =ItemTouchHelper(itemTouchHelper)
        swipe.attachToRecyclerView(reView)

        binding?.date?.setOnClickListener { view ->
            ClickDateListener(view)
        }
        binding?.dltBtn?.setOnClickListener {
            alertDialog()
        }
        binding?.newTransaction?.setOnClickListener {
            showDialog()

        }

        update()
    }


    private fun dltTransaction(trans : Transaction){
        dlttransac = trans
        oldtransac = transactions
        val ID = trans.id
        GlobalScope.launch {
            val helper = TransactionDatabase(applicationContext)
            val db = helper.readableDatabase
            db.execSQL("delete from record where id=$ID")
            runOnUiThread {
                update()
                showsnackbar()
            }
        }

    }

    private fun showsnackbar() {
        val v = findViewById<View>(R.id.coord)
        val s = Snackbar.make(v,"Transaction Deleted",Snackbar.LENGTH_LONG)
        s.setAction("Undo"){
            undoDlt()
        }.setActionTextColor(ContextCompat.getColor(this,R.color.red))
        .setTextColor(ContextCompat.getColor(this,R.color.white)).show()
    }
    private fun undoDlt(){
        adddata(dlttransac.label, dlttransac.id, dlttransac.price, dlttransac.date, "desc")
        update()
    }

    fun update(){
        transactions.clear()
        var helper = TransactionDatabase(applicationContext)
        var db = helper.readableDatabase
        val res = db.rawQuery("select * from record",null)
        while(res.moveToNext()){
            transactions.add(Transaction(res.getInt(0),res.getString(1),res.getDouble(2),res.getString(3),res.getString(4),res.getString(5)))
            Id= maxOf(Id,res.getInt(0))
        }
//        Toast.makeText(this,"count : ${transactions.size}", Toast.LENGTH_LONG).show()
        Transactionadapter = TransactionAdapter(transactions)
        layoutmanager = LinearLayoutManager(this)
        binding?.reView?.adapter = Transactionadapter
        binding?.reView?.layoutManager=layoutmanager
        updateDashBoard()
    }
    private fun updateDashBoard(){
        val budget : Double =transactions.map { it.price }.sum()
        var expenses : Double = transactions.filter { it.price>0 }.map { it.price }.sum()
        expenses=budget-expenses
        expenses*=-1
        binding?.actualBudget?.text = "Rs "+budget.toString()
        binding?.totalExpenses?.text="Rs "+expenses.toString()
    }
    private fun alertDialog(){
        val dialog = Dialog(this,R.style.CustomAlertDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setContentView(R.layout.alert_dialog)
        dialog.window?.setLayout(1000,600)
        dialog.deny.setOnClickListener {
            dialog.dismiss()
        }
        dialog.agree.setOnClickListener {
            val helper = TransactionDatabase(applicationContext)
            val db = helper.readableDatabase
            db.execSQL("delete from record")
            Toast.makeText(this,"Transactions Deleted",Toast.LENGTH_LONG).show()
            update()
            dialog.dismiss()
        }
        dialog.show()
    }
    fun adddata(name: String, id: Int, price: Double, date: String, desc: String){
        val helper = TransactionDatabase(applicationContext)
        val db = helper.readableDatabase
        lifecycleScope.launch {
            val cv = ContentValues()
            cv.put("id",id)
            cv.put("name",name)
            cv.put("price",price.toInt())
            cv.put("date",date)
            cv.put("time",currTime)
            cv.put("describe",desc)
            db.insert("record",null,cv)
        }
    }
    private fun showDialog(){
        val dialog = Dialog(this,R.style.CustomAlertDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(true)
//        dialog.setCancelable(false)
        dialog.setContentView(R.layout.activity_new_transaction)

        text=object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(dialog.labelInput.text.toString().isNotBlank() && dialog.amountInput.text.toString().isNotBlank()){
                    dialog.addTransac.visibility=View.GONE
                    dialog.addTransacVis.visibility=View.VISIBLE
                }else{
                    dialog.addTransac.visibility=View.VISIBLE
                    dialog.addTransacVis.visibility=View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if(dialog.labelInput.text.toString().isNotBlank() && dialog.amountInput.text.toString().isNotBlank()){
                    dialog.addTransac.visibility=View.GONE
                    dialog.addTransacVis.visibility=View.VISIBLE
                }else{
                    dialog.addTransac.visibility=View.VISIBLE
                    dialog.addTransacVis.visibility=View.GONE
                }
            }

        }
        neg=-1.0
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,1600)
        dialog.labelInput.addTextChangedListener(text)
        dialog.amountInput.addTextChangedListener(text)
        dialog.exp.setOnClickListener{
            neg=-1.0
        }
        dialog.recieve.setOnClickListener {
            neg=1.0
        }
        dialog.closebtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.addTransacVis.setOnClickListener {
            val name = dialog.labelInput.text.toString()
            val price = dialog.amountInput.text.toString()
            var desc = dialog.desInput.text.toString()
            if(desc.isBlank())
                desc="null"
            adddata(name,Id+1,neg*price.toDouble(),currDate,desc)
            Toast.makeText(this,"$name,$price,$currDate",Toast.LENGTH_LONG).show()
            Id++
            update()
            dialog.dismiss()
        }
        dialog.show()
    }
    fun ClickDateListener(view : View){
        val myCalendar = Calendar.getInstance()
        val year = myCalendar.get(Calendar.YEAR)
        val month = myCalendar.get(Calendar.MONTH)
        val dayOfMonth =myCalendar.get(Calendar.DAY_OF_MONTH)
        val dPicker = DatePickerDialog(this, DatePickerDialog.OnDateSetListener {
                view, Selectedyear, Selectedmonth, SelecteddayOfMonth ->
            Toast.makeText(this, "The chosen date is $SelecteddayOfMonth/${Selectedmonth+1}/$Selectedyear",
                Toast.LENGTH_LONG).show()


            val selectedDate ="$SelecteddayOfMonth/${Selectedmonth+1}/$Selectedyear"

            transactions.clear()
            val helper = TransactionDatabase(applicationContext)
            val db = helper.readableDatabase
            val res = db.rawQuery("select * from record where date='$selectedDate'",null)
            while(res.moveToNext()){
                transactions.add(Transaction(res.getInt(0),res.getString(1),res.getDouble(2),res.getString(3),res.getString(4),res.getString(5)))
            }
            Transactionadapter = TransactionAdapter(transactions)
            layoutmanager = LinearLayoutManager(this)
            binding?.reView?.adapter = Transactionadapter
            binding?.reView?.layoutManager=layoutmanager


        },year,month,dayOfMonth)
        dPicker.show()


    }
    private fun createnotification(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val name : CharSequence ="reminderChannel"
            val desc = "channel for notify"
            val import =NotificationManager.IMPORTANCE_HIGH
            val channel =NotificationChannel("expensemanage",name,import)
            channel.description=desc
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun setalarm(){
        alarmManager =getSystemService(ALARM_SERVICE) as AlarmManager
        calendar=Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY]=9
        calendar[Calendar.MINUTE]=9
        calendar[Calendar.MILLISECOND]=9
        val intent = Intent(this,alarmReciever::class.java)
        pendingIntent =PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_IMMUTABLE)
        alarmManager.set(
            AlarmManager.RTC_WAKEUP,calendar.timeInMillis
            ,pendingIntent
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        binding=null
    }
}