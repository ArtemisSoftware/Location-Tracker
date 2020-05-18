package com.artemissoftware.locationtracker.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.single.PermissionListener

class Permissions {

    companion object{


        /**
         * Return the current state of the permissions needed.
         */
        fun checkPermissions(context : Context) = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        fun requestPermissions(context : Context, listener : PermissionListener) {
            Dexter.withContext(context)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(listener)
                .check()
        }

    }

}