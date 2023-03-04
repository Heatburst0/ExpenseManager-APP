package com.kv.expensemanage

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import androidx.recyclerview.widget.RecyclerView
import com.kv.expensemanage.databinding.TransactionBinding
import kotlinx.android.synthetic.main.activity_new_transaction.*
import kotlinx.android.synthetic.main.activity_new_transaction.addTransacVis
import kotlinx.android.synthetic.main.activity_new_transaction.closebtn
import kotlinx.android.synthetic.main.activity_new_transaction.desInput
import kotlinx.android.synthetic.main.activity_new_transaction.exp
import kotlinx.android.synthetic.main.activity_new_transaction.recieve
import kotlinx.android.synthetic.main.detailtransac.*


class TransactionAdapter(var transactions : List<Transaction>) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {
    class ViewHolder(binding : TransactionBinding) : RecyclerView.ViewHolder(binding.root){
        val label = binding.itemName
        val price = binding.price
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(TransactionBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction : Transaction = transactions[position]
        val context  = holder.price.context
        if(transaction.price>0){
            holder.itemView.setBackgroundColor(Color.parseColor("#D6F9D2"))
            holder.price.text = "+ Rs%.2f".format(transaction.price)
        }else{
            holder.itemView.setBackgroundColor(Color.parseColor("#F9D2D2"))
            holder.price.text = "- Rs%.2f".format(transaction.price*(-1))
        }
        holder.label.text=transaction.label
        holder.itemView.setOnClickListener {
            showDialog(context,transaction)
        }
    }

    override fun getItemCount(): Int {
        return transactions.size

    }
    private fun showDialog(context: Context, transaction: Transaction){
        val dialog = Dialog(context,R.style.CustomAlertDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(true)
//        dialog.setCancelable(false)
        dialog.setContentView(R.layout.detailtransac)

        var neg=1.0


        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,1600)

        dialog.exp2.setOnClickListener{
            neg=-1.0
        }
        dialog.recieve2.setOnClickListener {
            neg=1.0
        }
        dialog.closebtn2.setOnClickListener {
            dialog.dismiss()
        }
        dialog.labelInput2.setText(transaction.label)
        dialog.amountInput2.setText(transaction.price.toString())
        dialog.dateText.setText(transaction.date)
        dialog.TimeText.setText(transaction.time)
        dialog.updateTrans.setOnClickListener {
            val name = dialog.labelInput2.text.toString()
            val price = dialog.amountInput2.text.toString()
            var desc = dialog.desInput2.text.toString()
            if(desc.isBlank())
                desc="null"

            dialog.dismiss()
        }
        dialog.show()
    }

}