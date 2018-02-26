package com.example.bartomiejjakubczak.camera

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val service = Intent(baseContext, CapPhoto::class.java)
//        val intent = PendingIntent.getService(this, 0, service, 0)
//        val alarm = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.SECOND, 15)
//        alarm.setRepeating(AlarmManager.RTC_WAKEUP,
//                calendar.timeInMillis,
//                60000,
//                intent)
        startService(service)
    }

}
