package com.kv.expensemanage

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class alarmReciever : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val i = Intent(context,MainActivity::class.java)
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent= PendingIntent.getActivity(context,0,i,0)

        val builder = NotificationCompat.Builder(context!!,"expensemanage")
            .setSmallIcon(R.mipmap.app_logo)
            .setContentTitle("Expense Manager")
            .setContentText("Update your transactions")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVibrate(longArrayOf(1000,1000,1000,1000))
            .setContentIntent(pendingIntent)
        val manager = NotificationManagerCompat.from(context)
        manager.notify(123,builder.build())

    }
}