package com.artemissoftware.locationtracker.util

import android.content.Context
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter
import android.content.Context.BATTERY_SERVICE
import androidx.core.content.ContextCompat.getSystemService
import android.os.Build



class Battery {

    companion object{


        fun getBatteryPercentage(context: Context): Int {

            if (Build.VERSION.SDK_INT >=  android.os.Build.VERSION_CODES.LOLLIPOP) {

                val bm = context.getSystemService(BATTERY_SERVICE) as BatteryManager
                return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

            }
            else {

                val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                val batteryStatus = context.registerReceiver(null, iFilter)

                val level = if (batteryStatus != null) batteryStatus!!.getIntExtra(BatteryManager.EXTRA_LEVEL,-1) else -1
                val scale = if (batteryStatus != null) batteryStatus!!.getIntExtra(BatteryManager.EXTRA_SCALE, -1) else -1

                val batteryPct = level / scale.toDouble()

                return (batteryPct * 100).toInt()
            }
        }
    }
}